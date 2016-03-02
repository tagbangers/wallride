package org.wallride.autoconfigure;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.boot.autoconfigure.MessageSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;
import org.thymeleaf.spring4.resourceresolver.SpringResourceResourceResolver;

@Configuration
@EnableConfigurationProperties(WallRideProperties.class)
@EnableAsync
@ComponentScan(basePackages="org.wallride.core", excludeFilters={ @ComponentScan.Filter(Configuration.class)} )
public class WallRideMessageSourceConfiguration extends MessageSourceAutoConfiguration {

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
	public AmazonS3 amazonS3() {
//		final String accessKey = environment.getRequiredProperty("aws.accessKey");
//		final String secretKey = environment.getRequiredProperty("aws.secretKey");
		ClientConfiguration configuration = new ClientConfiguration();
		configuration.setMaxConnections(1000);
//		return new AmazonS3Client(new BasicAWSCredentials(accessKey, secretKey), configuration);
		return new AmazonS3Client(configuration);
	}
}
