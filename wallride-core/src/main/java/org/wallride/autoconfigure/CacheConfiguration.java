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
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

@Configuration
@EnableCaching
public class CacheConfiguration {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private Environment environment;

	@Autowired
	private DataSourceProperties dataSourceProperties;

	private static Logger logger = LoggerFactory.getLogger(CacheConfiguration.class);

	@Bean
	public CacheManager cacheManager() throws Exception {
		// JGroups settings
		String jgroupsConfigurationFile = environment.getRequiredProperty("jgroups.configurationFile");
		if ("jgroups-ec2.xml".equals(jgroupsConfigurationFile)) {
			ClassConfigurator.addProtocol((short) 1000, S3_CLIENT_PING.class);
			EC2MetadataClient metadataClient = new EC2MetadataClient();
			String ipAddress = metadataClient.readResource("/latest/meta-data/local-ipv4");
			logger.info("jgroups.tcp.address -> {}", ipAddress);
			System.setProperty("jgroups.tcp.address", ipAddress);
			System.setProperty("jgroups.s3.bucket", environment.getRequiredProperty("jgroups.s3.bucket"));
		}

		Resource hibernateSearchConfig = new ClassPathResource(DefaultCacheManagerService.DEFAULT_INFINISPAN_CONFIGURATION_RESOURCENAME);
		ParserRegistry parserRegistry = new ParserRegistry();
		ConfigurationBuilderHolder holder = parserRegistry.parse(hibernateSearchConfig.getInputStream());

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
				.cacheMode(CacheMode.INVALIDATION_SYNC);
//				.indexing()
//					.index(Index.NONE);
		// @formatter:on

		holder.getNamedConfigurationBuilders().put("blogs", cacheBuilder);
//		holder.getNamedConfigurationBuilders().put("settings", cacheBuilder);
		holder.getNamedConfigurationBuilders().put("popularPosts", cacheBuilder);
		holder.getNamedConfigurationBuilders().put("articles", cacheBuilder);
		holder.getNamedConfigurationBuilders().put("categories", cacheBuilder);
		holder.getNamedConfigurationBuilders().put("pages", cacheBuilder);
		holder.getNamedConfigurationBuilders().put("medias", cacheBuilder);
		holder.getNamedConfigurationBuilders().put("banners", cacheBuilder);

		EmbeddedCacheManager embeddedCacheManager = new DefaultCacheManager(holder, true);
		InfinispanSingletonCacheManagerDirectoryProvider.cacheManager = embeddedCacheManager;
		return new SpringEmbeddedCacheManager(embeddedCacheManager);
	}
}
