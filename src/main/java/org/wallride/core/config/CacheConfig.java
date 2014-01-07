package org.wallride.core.config;

import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.loaders.jdbc.configuration.JdbcStringBasedCacheStoreConfigurationBuilder;
import org.infinispan.lucene.LuceneKey2StringMapper;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.query.indexmanager.InfinispanIndexManager;
import org.infinispan.spring.provider.SpringEmbeddedCacheManagerFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jndi.JndiTemplate;

import javax.inject.Inject;
import javax.naming.NamingException;
import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;

@Configuration
@EnableCaching
public class CacheConfig {

	@Inject
	private Environment environment;

	private static Logger logger = LoggerFactory.getLogger(CacheConfig.class);

	@Bean
	public CacheManager cacheManager() throws Exception {
//		TrustManager[] tm = { new X509TrustManager() {
//			public X509Certificate[] getAcceptedIssuers() {
//				return null;
//			}
//
//			@Override
//			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//			}
//
//			@Override
//			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//			}
//		}};
//		SSLContext sslcontext = SSLContext.getInstance("SSL");
//		sslcontext.init(null, tm, null);
//		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
//			@Override
//			public boolean verify(String hostname, SSLSession session) {
//				return true;
//			}
//		});

		// JGroups settings
		String ipaddress = null;
		try {
			ipaddress = InetAddress.getLocalHost().getHostAddress();
		}
		catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}

		String jgroupsConfigurationFile = environment.getRequiredProperty("jgroups.configurationFile");
		if ("jgroups-ec2.xml".equals(jgroupsConfigurationFile)) {
			System.setProperty("jgroups.s3.access_key", System.getProperty("AWS_ACCESS_KEY_ID"));
			System.setProperty("jgroups.s3.secret_access_key", System.getProperty("AWS_SECRET_KEY"));
			System.setProperty("jgroups.s3.bucket",  environment.getRequiredProperty("jgroups.s3.bucket"));
		}
		System.setProperty("jgroups.bind_addr", ipaddress);

		ConfigurationBuilderHolder holder = new ConfigurationBuilderHolder();

//		GlobalConfigurationBuilder globalBuilder = new GlobalConfigurationBuilder();
		GlobalConfigurationBuilder globalBuilder = holder.getGlobalConfigurationBuilder();
		globalBuilder
			.globalJmxStatistics()
				.enable()
				.cacheManagerName("HibernateSearch")
				.allowDuplicateDomains(true)
			.transport()
				.defaultTransport()
				.clusterName("wallride-cluster")
				.addProperty("configurationFile", jgroupsConfigurationFile);

		Properties props = new Properties();
		props.put("hibernate.search.default.directory_provider", "infinispan");
		props.put("hibernate.search.default.indexmanager", InfinispanIndexManager.class.getCanonicalName());
		props.put("hibernate.search.default.exclusive_index_use", "false");

		ConfigurationBuilder defaultBuilder = holder.getDefaultConfigurationBuilder();

//		JdbcStringBasedStoreConfigurationBuilder jdbcBuilder = new JdbcStringBasedStoreConfigurationBuilder(defaultBuilder.persistence());
		JdbcStringBasedCacheStoreConfigurationBuilder jdbcBuilder = new JdbcStringBasedCacheStoreConfigurationBuilder(defaultBuilder.loaders());
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
			.dataSource().jndiUrl("dataSource");

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
//			.persistence()
			.loaders()
				.preload(true)
				.passivation(false)
				.shared(true)
				.addStore(jdbcBuilder);
//				.preload(true)
//				.shared(true);

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
		cacheBuilder
			.clustering()
				.cacheMode(CacheMode.INVALIDATION_SYNC)
			.indexing()
				.enabled(false);
//			.persistence()
//				.clearStores();

		holder.getNamedConfigurationBuilders().put("settings", cacheBuilder);
		holder.getNamedConfigurationBuilders().put("articles", cacheBuilder);
		holder.getNamedConfigurationBuilders().put("categories", cacheBuilder);
		holder.getNamedConfigurationBuilders().put("pages", cacheBuilder);
		holder.getNamedConfigurationBuilders().put("medias", cacheBuilder);
		holder.getNamedConfigurationBuilders().put("banners", cacheBuilder);

//		holder.getNamedConfigurationBuilders().put("resources", cacheBuilder);

//		namedBuilder
//			.indexing()
//				.enable()
//				.indexLocalOnly(false)
//				.withProperties(props)
//			.persistence()
//				.addStore(jdbcBuilder)
//				.preload(true)
//				.shared(true)
//			.clustering()
//				.cacheMode(CacheMode.DIST_SYNC)
//			.jmxStatistics()
//				.enable();
//
//		holder.getNamedConfigurationBuilders().put("LuceneIndexesMetadata", namedBuilder);
//		holder.getNamedConfigurationBuilders().put("LuceneIndexesData", namedBuilder);
//		holder.getNamedConfigurationBuilders().put("LuceneIndexesLocking", namedBuilder);


//		Properties props = new Properties();
//		props.put("hibernate.search.default.directory_provider", "infinispan");
//		props.put("hibernate.search.default.indexmanager", InfinispanIndexManager.class.getCanonicalName());

//		ConfigurationBuilder cacheBuilder = new ConfigurationBuilder();
//		ConfigurationBuilder namedBuilder = new ConfigurationBuilder();
//		JdbcStringBasedStoreConfigurationBuilder jdbcBuilder = new JdbcStringBasedStoreConfigurationBuilder(namedBuilder.persistence());
//		jdbcBuilder
//			.key2StringMapper(LuceneKey2StringMapper.class)
//			.table()
//			.tableNamePrefix("ispn_string_table")
//			.idColumnName("id_column")
//			.idColumnType("varchar(255)")
//			.dataColumnName("data_column")
//			.dataColumnType("longblob")
//			.timestampColumnName("timestamp_column")
//			.timestampColumnType("bigint")
//			.dropOnExit(false)
//			.createOnStart(true)
//			.dataSource().jndiUrl("dataSource");
//		namedBuilder
//			.indexing()
//			.enable()
//			.indexLocalOnly(false)
//			.withProperties(props)
//			.persistence()
//				.addStore(jdbcBuilder)
//			.clustering()
//				.cacheMode(CacheMode.DIST_SYNC);
//
//		holder.getNamedConfigurationBuilders().put("LuceneIndexesMetadata", namedBuilder);
//		holder.getNamedConfigurationBuilders().put("LuceneIndexesData", namedBuilder);
//		holder.getNamedConfigurationBuilders().put("LuceneIndexesLocking", namedBuilder);


//		final EmbeddedCacheManager embeddedCacheManager = new DefaultCacheManager(globalBuilder.build(), cacheBuilder.build());
		final EmbeddedCacheManager embeddedCacheManager = new DefaultCacheManager(holder, true);




		SpringEmbeddedCacheManagerFactoryBean factory = new SpringEmbeddedCacheManagerFactoryBean() {
			@Override
			protected EmbeddedCacheManager createBackingEmbeddedCacheManager() throws IOException {
				return embeddedCacheManager;
			}
		};
		factory.afterPropertiesSet();
		CacheManager cacheManager = factory.getObject();

		try {
			JndiTemplate jndiTemplate = new JndiTemplate();
			jndiTemplate.bind("cacheManager", embeddedCacheManager);
		}
		catch (NamingException e) {
			logger.error("JNDI error.", e);
		}
		return cacheManager;

//		SimpleCacheManager cacheManager = new SimpleCacheManager();
//		cacheManager.setCaches(Arrays.asList(
//				new ConcurrentMapCache("setting"),
//				new ConcurrentMapCache("setting")));
//		return cacheManager;
	}
}
