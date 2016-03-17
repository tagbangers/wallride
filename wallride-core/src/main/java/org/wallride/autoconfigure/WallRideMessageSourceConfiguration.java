package org.wallride.autoconfigure;

import org.springframework.boot.autoconfigure.MessageSourceAutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;

@Configuration
public class WallRideMessageSourceConfiguration extends MessageSourceAutoConfiguration {

	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setDefaultEncoding("utf-8");
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
}
