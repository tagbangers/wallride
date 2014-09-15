package org.wallride.config;

import org.apache.commons.io.IOUtils;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.lucene.LuceneKey2StringMapper;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.persistence.jdbc.configuration.JdbcStringBasedStoreConfigurationBuilder;
import org.infinispan.query.indexmanager.InfinispanIndexManager;
import org.infinispan.spring.provider.SpringEmbeddedCacheManagerFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
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
			Process process = Runtime.getRuntime().exec("GET http://instance-data/latest/meta-data/local-ipv4");
			List<String> results = IOUtils.readLines(process.getInputStream());
			if (!CollectionUtils.isEmpty(results)) {
				String ipaddress = results.get(0);
				logger.info("jgroups.bind_addr -> {}", ipaddress);
				System.setProperty("jgroups.bind_addr", ipaddress);
			}
			System.setProperty("jgroups.s3.access_key", environment.getRequiredProperty("aws.accessKey"));
			System.setProperty("jgroups.s3.secret_access_key", environment.getRequiredProperty("aws.secretKey"));
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
		props.put("hibernate.search.default.indexmanager", InfinispanIndexManager.class.getCanonicalName());
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
			.locking()
				.lockAcquisitionTimeout(300000)
				.writeSkewCheck(false)
				.concurrencyLevel(500)
				.useLockStriping(false)
			.clustering()
				.cacheMode(CacheMode.DIST_SYNC)
			.stateTransfer()
				.timeout(960000)
				.fetchInMemoryState(true)
			.sync()
			.replTimeout(480000)
				.jmxStatistics()
				.enable()
			.eviction()
				.maxEntries(-1)
				.strategy(EvictionStrategy.NONE)
				.expiration()
					.maxIdle(-1)
					.reaperEnabled(false)
			.indexing()
				.enable()
				.indexLocalOnly(false)
				.withProperties(props)
			.persistence()
			.addStore(jdbcBuilder)
			.preload(true)
			.shared(true);
//			.loaders()
//				.preload(true)
//				.passivation(false)
//				.shared(true)
//				.addStore(jdbcBuilder);
//				.preload(true)
//				.shared(true);
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
				.enabled(false);
//			.persistence()
//				.clearStores();
		// @formatter:on

		holder.getNamedConfigurationBuilders().put("blogs", cacheBuilder);
		holder.getNamedConfigurationBuilders().put("settings", cacheBuilder);
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
