/*
 * Copyright 2014 Tagbangers, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wallride.autoconfigure;

import com.amazonaws.internal.EC2MetadataClient;
import jp.co.tagbangers.jgroups.S3_CLIENT_PING;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ParserRegistry;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.eviction.EvictionType;
import org.infinispan.hibernate.search.impl.DefaultCacheManagerService;
import org.infinispan.lucene.LuceneKey2StringMapper;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.persistence.jdbc.configuration.JdbcStringBasedStoreConfigurationBuilder;
import org.infinispan.spring.provider.SpringEmbeddedCacheManager;
import org.jgroups.conf.ClassConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@EnableCaching
public class WallRideCacheConfiguration extends CachingConfigurerSupport {

	public static final String BLOG_CACHE = "blogs";
	public static final String POPULAR_POST_CACHE = "popularPosts";
	public static final String ARTICLE_CACHE = "articles";
	public static final String PAGE_CACHE = "pages";
	public static final String CATEGORY_CACHE = "categories";
	public static final String CUSTOM_FIELD_CACHE = "customFields";
	public static final String MEDIA_CACHE = "medias";
	public static final String BANNER_CACHE = "banners";
	public static final String USER_CACHE = "users";

	@Autowired
	private DataSource dataSource;

	@Autowired
	private Environment environment;

	@Autowired
	private DataSourceProperties dataSourceProperties;

	private static Logger logger = LoggerFactory.getLogger(WallRideCacheConfiguration.class);

	@Bean
	@Override
	public CacheManager cacheManager() {
		// JGroups settings
		String jgroupsConfigurationFile = environment.getRequiredProperty("jgroups.configurationFile");
		if ("jgroups-ec2.xml".equals(jgroupsConfigurationFile)) {
			ClassConfigurator.addProtocol((short) 1000, S3_CLIENT_PING.class);
			EC2MetadataClient metadataClient = new EC2MetadataClient();
			String ipAddress;
			try {
				ipAddress = metadataClient.readResource("/latest/meta-data/local-ipv4");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			logger.info("jgroups.tcp.address -> {}", ipAddress);
			System.setProperty("jgroups.tcp.address", ipAddress);
			System.setProperty("jgroups.s3.bucket", environment.getRequiredProperty("jgroups.s3.bucket"));
		}

		Resource hibernateSearchConfig = new ClassPathResource(DefaultCacheManagerService.DEFAULT_INFINISPAN_CONFIGURATION_RESOURCENAME);
		ParserRegistry parserRegistry = new ParserRegistry();
		ConfigurationBuilderHolder holder;
		try {
			holder = parserRegistry.parse(hibernateSearchConfig.getInputStream());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// GlobalConfiguration
		// @formatter:off
		GlobalConfigurationBuilder globalBuilder = holder.getGlobalConfigurationBuilder();
		globalBuilder
			.globalJmxStatistics()
				.allowDuplicateDomains(true)
			.transport()
				.defaultTransport()
					.addProperty("configurationFile", jgroupsConfigurationFile);
		// @formatter:on

		// DefaultConfiguration
		// @formatter:off
		for (ConfigurationBuilder luceneIndexesBuilder : holder.getNamedConfigurationBuilders().values()) {
			if ("mysql".equals(dataSourceProperties.getPlatform())) {
				luceneIndexesBuilder
					.persistence()
						.addStore(JdbcStringBasedStoreConfigurationBuilder.class)
							.preload(true)
							.shared(true)
							.key2StringMapper(LuceneKey2StringMapper.class)
								.table()
								.tableNamePrefix("ispn_string_table")
								.idColumnName("id_column")
								.idColumnType("varchar(255)")
								.dataColumnName("data_column")
								.dataColumnType("longblob")
								.timestampColumnName("timestamp_column")
								.timestampColumnType("bigint")
								.dropOnExit(false)
								.createOnStart(true)
							.async()
								.enable()
								.threadPoolSize(10)
							.fetchPersistentState(true)
							.ignoreModifications(false)
							.purgeOnStartup(false)
							.connectionFactory(InfinispanDataSourceConnectionFactoryConfigurationBuilder.class).dataSource(dataSource);
			} else if ("postgresql".equals(dataSourceProperties.getPlatform())) {
				luceneIndexesBuilder
					.persistence()
						.addStore(JdbcStringBasedStoreConfigurationBuilder.class)
							.preload(true)
							.shared(true)
							.key2StringMapper(LuceneKey2StringMapper.class)
								.table()
								.tableNamePrefix("ispn_string_table")
								.idColumnName("id_column")
								.idColumnType("varchar(255)")
								.dataColumnName("data_column")
								.dataColumnType("bytea")
								.timestampColumnName("timestamp_column")
								.timestampColumnType("bigint")
								.dropOnExit(false)
								.createOnStart(true)
							.async()
								.enable()
								.threadPoolSize(10)
							.fetchPersistentState(true)
							.ignoreModifications(false)
							.purgeOnStartup(false)
							.connectionFactory(InfinispanDataSourceConnectionFactoryConfigurationBuilder.class).dataSource(dataSource);
			} else {
				throw new IllegalStateException();
			}
		}
		// @formatter:on

		// @formatter:off
		ConfigurationBuilder cacheBuilder = new ConfigurationBuilder();
		cacheBuilder
			.clustering()
				.cacheMode(CacheMode.REPL_SYNC)
				.eviction()
					.type(EvictionType.COUNT)
					.strategy(EvictionStrategy.LIRS)
					.size(1000);
//				.indexing()
//					.index(Index.NONE);
		// @formatter:on

		holder.getNamedConfigurationBuilders().put(BLOG_CACHE, cacheBuilder);
//		holder.getNamedConfigurationBuilders().put("settings", cacheBuilder);
		holder.getNamedConfigurationBuilders().put(POPULAR_POST_CACHE, cacheBuilder);
		holder.getNamedConfigurationBuilders().put(ARTICLE_CACHE, cacheBuilder);
		holder.getNamedConfigurationBuilders().put(PAGE_CACHE, cacheBuilder);
		holder.getNamedConfigurationBuilders().put(CATEGORY_CACHE, cacheBuilder);
		holder.getNamedConfigurationBuilders().put(CUSTOM_FIELD_CACHE, cacheBuilder);
		holder.getNamedConfigurationBuilders().put(MEDIA_CACHE, cacheBuilder);
		holder.getNamedConfigurationBuilders().put(BANNER_CACHE, cacheBuilder);
		holder.getNamedConfigurationBuilders().put(USER_CACHE, cacheBuilder);

		EmbeddedCacheManager embeddedCacheManager = new DefaultCacheManager(holder, true);
		InfinispanSingletonCacheManagerDirectoryProvider.cacheManager = embeddedCacheManager;
		return new SpringEmbeddedCacheManager(embeddedCacheManager);
	}

	@Bean
	@Override
	public KeyGenerator keyGenerator() {
		return new CacheKeyGenerator();
	}
}
