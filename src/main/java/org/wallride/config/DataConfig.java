package org.wallride.config;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import org.wallride.core.domain.DomainObject;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

@Configuration
@EnableJpaRepositories("org.wallride.core.repository")
@EnableTransactionManagement
@EnableBatchProcessing
public class DataConfig implements BatchConfigurer {

	private static Logger logger = LoggerFactory.getLogger(DataConfig.class);

	@Inject
	private ResourceLoader resourceLoader;

	@Inject
	private Environment environment;

	@Value("classpath:create-table.sql")
	private Resource createTableScript;

	@Override
	public JobRepository getJobRepository() throws Exception {
		JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
		factory.setDataSource(dataSource());
		factory.setTransactionManager(getTransactionManager());
		factory.setIsolationLevelForCreate("ISOLATION_DEFAULT");
		factory.setValidateTransactionState(false);
		factory.afterPropertiesSet();
		return (JobRepository) factory.getObject();
	}

	@Override
	public JobLauncher getJobLauncher() throws Exception {
		SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
		jobLauncher.setJobRepository(getJobRepository());
		jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
		jobLauncher.afterPropertiesSet();
		return jobLauncher;
	}

	@Override
	public JobExplorer getJobExplorer() throws Exception {
		JobExplorerFactoryBean factory = new JobExplorerFactoryBean();
		factory.setDataSource(dataSource());
		factory.afterPropertiesSet();
		return factory.getObject();
	}

	@Override
	public PlatformTransactionManager getTransactionManager() throws UnsupportedEncodingException {
		JpaTransactionManager bean = new JpaTransactionManager();
		bean.setEntityManagerFactory(entityManagerFactory().getObject());
		return bean;
	}

	// additional data-related beans
	
	@Bean
	public DataSource dataSource() throws UnsupportedEncodingException {
		String jdbcConnectionString = environment.getRequiredProperty("jdbc.connection.string");
		UriComponents jdbcUriComponents = UriComponentsBuilder.fromUriString(jdbcConnectionString.substring("jdbc:".length())).build();

		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(environment.getRequiredProperty("jdbc.driver"));
		dataSource.setUsername(UriUtils.decode(jdbcUriComponents.getQueryParams().getFirst("user"), "UTF-8"));
		dataSource.setPassword(UriUtils.decode(jdbcUriComponents.getQueryParams().getFirst("password"), "UTF-8"));
		dataSource.setUrl(jdbcConnectionString.substring(0, jdbcConnectionString.indexOf("?")));

		dataSource.setMaxActive(environment.getRequiredProperty("datasource.maxActive", Integer.class));
		dataSource.setMaxIdle(environment.getRequiredProperty("datasource.maxIdle", Integer.class));
		dataSource.setTimeBetweenEvictionRunsMillis(environment.getRequiredProperty("datasource.timeBetweenEvictionRunsMillis", Long.class));
		dataSource.setTestWhileIdle(environment.getRequiredProperty("datasource.testWhileIdle", Boolean.class));
		dataSource.setValidationQuery(environment.getRequiredProperty("datasource.validationQuery"));
		dataSource.setMinEvictableIdleTimeMillis(environment.getRequiredProperty("datasource.minEvictableIdleTimeMillis", Long.class));
		dataSource.setNumTestsPerEvictionRun(environment.getRequiredProperty("datasource.numTestsPerEvictionRun", Integer.class));
		
		return dataSource;
	}

	@Bean
	public DataSourceInitializer dataSourceInitializer() throws UnsupportedEncodingException {
		final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
		populator.addScript(createTableScript);
		populator.setContinueOnError(true);

		final DataSourceInitializer initializer = new DataSourceInitializer();
		initializer.setDataSource(dataSource());
		initializer.setDatabasePopulator(populator);
		return initializer;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws UnsupportedEncodingException {
		LocalContainerEntityManagerFactoryBean entityManager = new LocalContainerEntityManagerFactoryBean();
		entityManager.setDataSource(dataSource());
		entityManager.setPackagesToScan(DomainObject.class.getPackage().getName());
		
		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setDatabase(Database.MYSQL);
		entityManager.setJpaVendorAdapter(vendorAdapter);
		
		Properties properties = new Properties();
		properties.put("hibernate.dialect", ExtendedMySQL5InnoDBDialect.class.getCanonicalName());
		properties.put("hibernate.show_sql", environment.getRequiredProperty("hibernate.show_sql"));
		properties.put("hibernate.format_sql", environment.getRequiredProperty("hibernate.format_sql"));

		// Hibernate Search
		properties.put("hibernate.search.lucene_version", environment.getRequiredProperty("hibernate.search.lucene_version"));
		properties.put("hibernate.search.analyzer", environment.getRequiredProperty("hibernate.search.analyzer"));

		properties.put("hibernate.search.infinispan.cachemanager_jndiname", environment.getRequiredProperty("hibernate.search.infinispan.cachemanager_jndiname"));
		properties.put("hibernate.search.infinispan.configuration_resourcename", environment.getRequiredProperty("hibernate.search.infinispan.configuration_resourcename"));
		properties.put("hibernate.search.default.directory_provider", environment.getRequiredProperty("hibernate.search.default.directory_provider"));
		properties.put("hibernate.search.default.indexBase", environment.getRequiredProperty("hibernate.search.default.indexBase"));
		properties.put("hibernate.search.default.exclusive_index_use", environment.getRequiredProperty("hibernate.search.default.exclusive_index_use"));
		entityManager.setJpaProperties(properties);
		
		return entityManager;
	}
}