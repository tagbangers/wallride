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
import org.thymeleaf.spring4.resourceresolver.SpringResourceResourceResolver;

import javax.inject.Inject;

@Configuration
@EnableAsync
@ComponentScan(basePackages="org.wallride.core", excludeFilters={ @Filter(Configuration.class)} )
public class CoreConfig {

	@Inject
	private Environment environment;

//	@Configuration
//	@Profile("default")
//	@PropertySource("classpath:application-default.properties")
//	static class Default extends PropertySourcesPlaceholderConfigurer {
//	}
//
//
//	@Configuration
//	@Profile("develop")
//	@PropertySource("classpath:application-develop.properties")
//	static class Develop extends PropertySourcesPlaceholderConfigurer {
//	}

//	@Configuration
//	@Profile("junit")
//	@PropertySource("classpath:environment-junit.properties")
//	static class Junit extends PropertySourcesPlaceholderConfigurer {
//	}

	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasenames(
				"classpath:/messages/messages",
				"classpath:/messages/validations",
				"classpath:/messages/enumerations",
				"classpath:/messages/languages"
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
	public SpringResourceResourceResolver springResourceResourceResolver() {
		return new SpringResourceResourceResolver();
	}

	@Bean
	@Lazy
	public AmazonS3Client amazonS3Client() {
		final String accessKey = environment.getRequiredProperty("aws.accessKey");
		final String secretKey = environment.getRequiredProperty("aws.secretKey");
		ClientConfiguration configuration = new ClientConfiguration();
		configuration.setMaxConnections(1000);
		return new AmazonS3Client(new BasicAWSCredentials(accessKey, secretKey), configuration);
	}
}