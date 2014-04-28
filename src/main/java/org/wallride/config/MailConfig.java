package org.wallride.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.spring3.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;
import org.wallride.core.support.CustomThymeleafDialect;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

@Configuration
public class MailConfig {

	@Inject
	private CustomThymeleafDialect customThymeleafDialect;

	@Inject
	private Environment environment;

	@Bean
	public JavaMailSender mailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(environment.getRequiredProperty("mail.smtp.host"));
		mailSender.setPort(Integer.parseInt(environment.getRequiredProperty("mail.smtp.port")));
		mailSender.setUsername(environment.getRequiredProperty("mail.smtp.username"));
		mailSender.setPassword(environment.getRequiredProperty("mail.smtp.password"));

		Properties props = new Properties();
		props.put("mail.smtp.auth", environment.getRequiredProperty("mail.smtp.auth"));
		props.put("mail.smtp.starttls.enable", environment.getRequiredProperty("mail.smtp.starttls.enable"));
		props.put("mail.smtp.starttls.required", environment.getRequiredProperty("mail.smtp.starttls.required"));
		props.put("mail.smtp.from", environment.getRequiredProperty("mail.smtp.from"));
		props.put("mail.from", environment.getRequiredProperty("mail.from"));
		props.put("mail.smtp.localhost", environment.getRequiredProperty("mail.smtp.localhost"));
		mailSender.setJavaMailProperties(props);

		return mailSender;
	}

	@Bean(name="emailTemplateResolver")
	public TemplateResolver emailTemplateResolver() {
		ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
		resolver.setPrefix(environment.getRequiredProperty("template.mail.path"));
		resolver.setSuffix(".html");
		resolver.setCharacterEncoding("UTF-8");
		// NB, selecting HTML5 as the template mode.
		resolver.setTemplateMode("HTML5");
		resolver.setCacheable(environment.getRequiredProperty("template.mail.cache", Boolean.class));
		resolver.setOrder(1);
		return resolver;
	}

	@Bean(name="emailTemplateEngine")
	public SpringTemplateEngine emailTemplateEngine() {
		SpringTemplateEngine engine = new SpringTemplateEngine();
		Set<TemplateResolver> resolvers = new HashSet<>();
		resolvers.add(emailTemplateResolver());
		engine.setTemplateResolvers(resolvers);

		Set<IDialect> dialects = new HashSet<>();
		dialects.add(customThymeleafDialect);
		engine.setAdditionalDialects(dialects);
		return engine;
	}
}
