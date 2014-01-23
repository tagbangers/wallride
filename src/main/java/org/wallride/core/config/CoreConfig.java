package org.wallride.core.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import javax.annotation.PostConstruct;

@Configuration
@ComponentScan(basePackages="org.wallride.core", excludeFilters={ @Filter(Configuration.class)} )
public class CoreConfig {
	
	@PostConstruct
	public void init() {
		// JGroups settings
		/*		String ipaddress = null;
		try {
			ipaddress = InetAddress.getLocalHost().getHostAddress();
		}
		catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		System.setProperty("jgroups.s3.access_key", environment.getRequiredProperty("amazon.s3.accesskey"));
		System.setProperty("jgroups.s3.secret_access_key", environment.getRequiredProperty("amazon.s3.secretkey"));
		System.setProperty("jgroups.s3.bucket", environment.getRequiredProperty("amazon.s3.bucket"));
		System.setProperty("jgroups.bind_addr", ipaddress);
*/	}
	
	@Configuration
	@Profile("develop")
	@PropertySource("classpath:environment-develop.properties")
	static class Develop extends PropertySourcesPlaceholderConfigurer {
	}

//	@Configuration
//	@Profile("test")
//	@PropertySource("classpath:environment-test.properties")
//	static class Test extends PropertySourcesPlaceholderConfigurer {
//	}

	@Configuration
	@Profile("product")
	@PropertySource("classpath:environment-product.properties")
	static class Product extends PropertySourcesPlaceholderConfigurer {
	}

	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasenames(
				"/WEB-INF/messages/messages",
				"/WEB-INF/messages/validations",
				"/WEB-INF/messages/enumerations",
				"/WEB-INF/messages/languages"
		);
		return messageSource;
	}
	
	@Bean
	public MessageSourceAccessor messageSourceAccessor() {
		MessageSourceAccessor messageSourceAccessor = new MessageSourceAccessor(messageSource());
		return messageSourceAccessor;
	}

/*	@Bean
	public AmazonS3Client amazonS3Client() {
		ClientConfiguration configuration = new ClientConfiguration();
		configuration.setMaxConnections(1000);
		return new AmazonS3Client(new BasicAWSCredentials(
				environment.getProperty("amazon.s3.accesskey"),
				environment.getProperty("amazon.s3.secretkey")),
				configuration);
	}
*/	
}