package org.wallride.autoconfigure;

import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;

@Configuration
public class WallRideMessageSourceConfiguration extends MessageSourceAutoConfiguration {

	@Override
	public MessageSource messageSource() {
		ResourceBundleMessageSource messageSource = (ResourceBundleMessageSource) super.messageSource();
		messageSource.addBasenames(
				"messages/messages",
				"messages/validations",
				"messages/enumerations",
				"messages/languages"
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
