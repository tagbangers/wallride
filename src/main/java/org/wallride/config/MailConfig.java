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

//	@Inject
//	private Settings settings;

	@Inject
	private Environment environment;

	@Bean
	public JavaMailSender mailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

		Properties props = new Properties();
		props.put("mail.smtp.host", environment.getRequiredProperty("smtp.host"));
		props.put("mail.from", environment.getRequiredProperty("smtp.from"));
		props.put("mail.smtp.from", environment.getRequiredProperty("smtp.from"));
//		props.put("mail.smtp.host", settings.readSettingAsString(Setting.Key.MAIL_SMTP_HOST));
//		props.put("mail.from", settings.readSettingAsString(Setting.Key.MAIL_FROM));
//		props.put("mail.smtp.from", settings.readSettingAsString(Setting.Key.MAIL_FROM));
		mailSender.setJavaMailProperties(props);

//		mailSender.setHost("");
//		mailSender.setPort(0);
//		mailSender.setProtocol("");
//		mailSender.setUsername("");
//		mailSender.setPassword("");
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
