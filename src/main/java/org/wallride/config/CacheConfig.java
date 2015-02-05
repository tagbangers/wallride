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

package org.wallride.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.internal.EC2MetadataClient;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.Index;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.lucene.LuceneKey2StringMapper;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.persistence.jdbc.configuration.JdbcStringBasedStoreConfigurationBuilder;
import org.infinispan.spring.provider.SpringEmbeddedCacheManagerFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

@Configuration
@EnableCaching
public class CacheConfig {

	@Inject
	private DataSource dataSource;

	@Inject
	private Environment environment;

	private static Logger logger = LoggerFactory.getLogger(CacheConfig.class);

	@Bean
	public SpringEmbeddedCacheManagerFactoryBean cacheManagerFactoryBean() throws Exception {
		// JGroups settings
		String jgroupsConfigurationFile = environment.getRequiredProperty("jgroups.configurationFile");
		if ("jgroups-ec2.xml".equals(jgroupsConfigurationFile)) {
			EC2MetadataClient metadataClient = new EC2MetadataClient();
			String ipaddress = metadataClient.readResource("/latest/meta-data/local-ipv4");
			logger.info("jgroups.tcp.address -> {}", ipaddress);
			System.setProperty("jgroups.tcp.address", ipaddress);

//			AWSCredentials awsCredentials = new DefaultAWSCredentialsProviderChain().getCredentials();
			AWSCredentials awsCredentials = new SystemPropertiesCredentialsProvider().getCredentials(); // TODO
			System.setProperty("jgroups.s3.access_key", awsCredentials.getAWSAccessKeyId());
			System.setProperty("jgroups.s3.secret_access_key", awsCredentials.getAWSSecretKey());
			System.setProperty("jgroups.s3.bucket",  environment.getRequiredProperty("jgroups.s3.bucket"));
		}

		ConfigurationBuilderHolder holder = new ConfigurationBuilderHolder();

		GlobalConfigurationBuilder globalBuilder = holder.getGlobalConfigurationBuilder();
		// @formatter:off
		globalBuilder
			.globalJmxStatistics()
				.enable()
//				.cacheManagerName("HibernateSearch")
				.allowDuplicateDomains(true)
			.transport()
				.defaultTransport()
//				.clusterName("wallride-cluster")
				.addProperty("configurationFile", jgroupsConfigurationFile);
		// @formatter:on

		Properties props = new Properties();
		props.put("hibernate.search.default.directory_provider", "infinispan");
//		props.put("hibernate.search.default.indexmanager", InfinispanIndexManager.class.getCanonicalName());
		props.put("hibernate.search.default.indexmanager", "near-real-time");
		props.put("hibernate.search.default.exclusive_index_use", "false");

		ConfigurationBuilder defaultBuilder = holder.getDefaultConfigurationBuilder();

		JdbcStringBasedStoreConfigurationBuilder jdbcBuilder = new JdbcStringBasedStoreConfigurationBuilder(defaultBuilder.persistence());
//		JdbcStringBasedCacheStoreConfigurationBuilder jdbcBuilder = new JdbcStringBasedCacheStoreConfigurationBuilder(defaultBuilder.loaders());
		// @formatter:off
		jdbcBuilder
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
				.flushLockTimeout(15000)
				.threadPoolSize(10)
			.fetchPersistentState(true)
			.ignoreModifications(false)
			.purgeOnStartup(false)
//			.dataSource().jndiUrl("dataSource");
			.connectionFactory(InfinispanDataSourceConnectionFactoryConfigurationBuilder.class).dataSource(dataSource);
		// @formatter:on

		// @formatter:off
		defaultBuilder
			.clustering()
				.cacheMode(CacheMode.DIST_SYNC)
//			.locking()
//				.lockAcquisitionTimeout(300000)
//				.writeSkewCheck(false)
//				.concurrencyLevel(500)
//				.useLockStriping(false)
//			.stateTransfer()
//				.timeout(960000)
//				.fetchInMemoryState(true)
//			.sync()
//			.replTimeout(480000)
//				.jmxStatistics()
//				.enable()
//			.eviction()
//				.maxEntries(-1)
//				.strategy(EvictionStrategy.NONE)
//				.expiration()
//					.maxIdle(-1)
//					.reaperEnabled(false)
//			.indexing()
//				.index(Index.LOCAL)
//				.enable()
//				.indexLocalOnly(false)
//				.withProperties(props)
			.persistence()
				.addStore(jdbcBuilder)
				.preload(true)
				.shared(true);
		// @formatter:on

//		ConfigurationBuilder luceneBuilder = new ConfigurationBuilder();
//		luceneBuilder
//			.clustering()
//				.cacheMode(CacheMode.REPL_SYNC)
//				.stateTransfer()
//					.fetchInMemoryState(true)
//				.sync()
//					.replTimeout(480000)
//			.indexing()
//					.enabled(false);
//
//		holder.getNamedConfigurationBuilders().put("LuceneIndexesMetadata", luceneBuilder);
//		holder.getNamedConfigurationBuilders().put("LuceneIndexesData", luceneBuilder);
//		holder.getNamedConfigurationBuilders().put("LuceneIndexesLocking", luceneBuilder);

		ConfigurationBuilder cacheBuilder = new ConfigurationBuilder();
		// @formatter:off
		cacheBuilder
			.clustering()
				.cacheMode(CacheMode.INVALIDATION_SYNC)
				.indexing()
					.index(Index.NONE);
//			.persistence()
//				.clearStores();
		// @formatter:on

		holder.getNamedConfigurationBuilders().put("blogs", cacheBuilder);
//		holder.getNamedConfigurationBuilders().put("settings", cacheBuilder);
		holder.getNamedConfigurationBuilders().put("popularPosts", cacheBuilder);
		holder.getNamedConfigurationBuilders().put("articles", cacheBuilder);
		holder.getNamedConfigurationBuilders().put("categories", cacheBuilder);
		holder.getNamedConfigurationBuilders().put("pages", cacheBuilder);
		holder.getNamedConfigurationBuilders().put("medias", cacheBuilder);
		holder.getNamedConfigurationBuilders().put("banners", cacheBuilder);

//		holder.getNamedConfigurationBuilders().put("resources", cacheBuilder);

//		final EmbeddedCacheManager embeddedCacheManager = new DefaultCacheManager(globalBuilder.build(), cacheBuilder.build());
		final EmbeddedCacheManager embeddedCacheManager = new DefaultCacheManager(holder, true);

		InfinispanSingletonCacheManagerDirectoryProvider.cacheManager = embeddedCacheManager;

		SpringEmbeddedCacheManagerFactoryBean factory = new SpringEmbeddedCacheManagerFactoryBean() {
			@Override
			protected EmbeddedCacheManager createBackingEmbeddedCacheManager() throws IOException {
				return embeddedCacheManager;
			}
		};
		return factory;
	}
}
