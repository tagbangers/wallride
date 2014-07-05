package org.wallride.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;

import javax.inject.Inject;

@Configuration
@EnableAsync
@ComponentScan(basePackages="org.wallride.core", excludeFilters={ @Filter(Configuration.class)} )
public class CoreConfig {

	@Inject
	private Environment environment;

	@Configuration
	@Profile("default")
	@PropertySource("classpath:environment-default.properties")
	static class Default extends PropertySourcesPlaceholderConfigurer {
	}


	@Configuration
	@Profile("develop")
	@PropertySource("classpath:environment-develop.properties")
	static class Develop extends PropertySourcesPlaceholderConfigurer {
	}

//	@Configuration
//	@Profile("junit")
//	@PropertySource("classpath:environment-junit.properties")
//	static class Junit extends PropertySourcesPlaceholderConfigurer {
//	}

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

	@Bean
	public MessageCodesResolver messageCodesResolver() {
		DefaultMessageCodesResolver resolver = new DefaultMessageCodesResolver();
		resolver.setPrefix("validation.");
		return resolver;
	}

	@Bean
	@Lazy
	public AmazonS3Client amazonS3Client() {
		final String accessKey = environment.getRequiredProperty("amazon.accesskey");
		final String secretKey = environment.getRequiredProperty("amazon.secretkey");
		ClientConfiguration configuration = new ClientConfiguration();
		configuration.setMaxConnections(1000);
		return new AmazonS3Client(new BasicAWSCredentials(accessKey, secretKey), configuration);
	}
}