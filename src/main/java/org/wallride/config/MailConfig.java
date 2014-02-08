package org.wallride.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.wallride.domain.Setting;
import org.wallride.support.Settings;

import javax.inject.Inject;
import java.util.Properties;

@Configuration
@Lazy
public class MailConfig {

//	@Inject
//	private Environment environment;

	@Inject
	private Settings settings;

	@Bean
	public JavaMailSender mailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

		Properties props = new Properties();
//		props.put("mail.smtp.host", environment.getRequiredProperty("mail.smtp.host"));
//		props.put("mail.from", environment.getRequiredProperty("mail.from"));
//		props.put("mail.smtp.from", environment.getRequiredProperty("mail.smtp.from"));
		props.put("mail.smtp.host", settings.readSettingAsString(Setting.Key.MAIL_SMTP_HOST));
		props.put("mail.from", settings.readSettingAsString(Setting.Key.MAIL_FROM));
		props.put("mail.smtp.from", settings.readSettingAsString(Setting.Key.MAIL_FROM));
		mailSender.setJavaMailProperties(props);

//		mailSender.setHost("");
//		mailSender.setPort(0);
//		mailSender.setProtocol("");
//		mailSender.setUsername("");
//		mailSender.setPassword("");
		return mailSender;
	}
}
