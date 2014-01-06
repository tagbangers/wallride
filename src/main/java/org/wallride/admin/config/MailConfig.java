package org.wallride.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.inject.Inject;
import java.util.Properties;

@Configuration
public class MailConfig {

	@Inject
	private Environment environment;

	@Bean
	public JavaMailSender mailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

		Properties props = new Properties();
		props.put("mail.smtp.host", environment.getRequiredProperty("mail.smtp.host"));
		props.put("mail.from", environment.getRequiredProperty("mail.from"));
		props.put("mail.smtp.from", environment.getRequiredProperty("mail.smtp.from"));
		mailSender.setJavaMailProperties(props);

//		mailSender.setHost("");
//		mailSender.setPort(0);
//		mailSender.setProtocol("");
//		mailSender.setUsername("");
//		mailSender.setPassword("");
		return mailSender;
	}
}
