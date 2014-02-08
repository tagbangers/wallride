package org.wallride.config;

import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.dialect.MySQL5InnoDBDialect;
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
import org.springframework.jndi.JndiTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.wallride.domain.DomainObject;

import javax.inject.Inject;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableJpaRepositories("org.wallride.repository")
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
	public PlatformTransactionManager getTransactionManager() {
		JpaTransactionManager bean = new JpaTransactionManager();
		bean.setEntityManagerFactory(entityManagerFactory().getObject());
		return bean;
	}

	// additional data-related beans
	
	@Bean
	public DataSource dataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(environment.getRequiredProperty("datasource.jdbc.driver"));
		dataSource.setUsername(environment.getRequiredProperty("datasource.jdbc.username"));
		dataSource.setPassword(environment.getRequiredProperty("datasource.jdbc.password"));
		dataSource.setUrl(environment.getRequiredProperty("datasource.jdbc.url"));
		dataSource.setMaxActive(environment.getRequiredProperty("datasource.maxActive", Integer.class));
		dataSource.setMaxIdle(environment.getRequiredProperty("datasource.maxIdle", Integer.class));
		dataSource.setTimeBetweenEvictionRunsMillis(environment.getRequiredProperty("datasource.timeBetweenEvictionRunsMillis", Long.class));
		dataSource.setTestWhileIdle(environment.getRequiredProperty("datasource.testWhileIdle", Boolean.class));
		dataSource.setValidationQuery(environment.getRequiredProperty("datasource.validationQuery"));
		dataSource.setMinEvictableIdleTimeMillis(environment.getRequiredProperty("datasource.minEvictableIdleTimeMillis", Long.class));
		dataSource.setNumTestsPerEvictionRun(environment.getRequiredProperty("datasource.numTestsPerEvictionRun", Integer.class));
		
		try {
			JndiTemplate jndiTemplate = new JndiTemplate();
			jndiTemplate.bind("dataSource", dataSource);
		}
		catch (NamingException e) {
			logger.error("JNDI error.", e);
		}

//		try {
//			Connection connection = dataSource.getConnection();
//			DatabaseMetaData metaData = connection.getMetaData();
//			ResultSet resultSet = metaData.getTables(null, null, "global_setting", new String[]{"TABLE"});
//			boolean created = (resultSet.next());
//			resultSet.close();
//
//			if (!created) {
//				File createTableSqlFile = resourceLoader.getResource("classpath:/create-table.sql").getFile();
//				connection.createStatement().executeUpdate(FileUtils.readFileToString(createTableSqlFile));
//			}
//			connection.close();
//		}
//		catch (Exception e) {
//			throw new BeanInitializationException("Failed to initialize the DataSource", e);
//		}

		return dataSource;
	}

	@Bean
	public DataSourceInitializer dataSourceInitializer() {
		final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
		populator.addScript(createTableScript);
		populator.setContinueOnError(true);

		final DataSourceInitializer initializer = new DataSourceInitializer();
		initializer.setDataSource(dataSource());
		initializer.setDatabasePopulator(populator);
		return initializer;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean entityManager = new LocalContainerEntityManagerFactoryBean();
		entityManager.setDataSource(dataSource());
		entityManager.setPackagesToScan(DomainObject.class.getPackage().getName());
		
		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setDatabase(Database.MYSQL);
		entityManager.setJpaVendorAdapter(vendorAdapter);
		
		Properties properties = new Properties();
		properties.put("hibernate.dialect", MySQL5InnoDBDialect.class.getCanonicalName());
		
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
	
	@Bean
	public JobExplorer jobExplorer() throws Exception {
		JobExplorerFactoryBean factory = new JobExplorerFactoryBean();
		factory.setDataSource(dataSource());
		factory.afterPropertiesSet();
		return (JobExplorer) factory.getObject();
	}
}