/*
 * Copyright 2014 Tagbangers, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wallride.autoconfigure;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import javax.inject.Inject;
import javax.mail.Session;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class WallRideMailConfiguration extends MailSenderAutoConfiguration {

	@Inject
	private ApplicationContext applicationContext;

	@Inject
	private WallRideThymeleafDialect wallRideThymeleafDialect;

	@Inject
	private Environment environment;

	@Inject
	private ThymeleafProperties properties;

	public WallRideMailConfiguration(MailProperties properties, ObjectProvider<Session> sessionProvider) {
		super(properties, sessionProvider);
	}

	@Bean(name = "emailTemplateResolver")
	public ITemplateResolver emailTemplateResolver() {
		SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
//		resolver.setResourceResolver(wallRideResourceResourceResolver);
		resolver.setApplicationContext(applicationContext);
		resolver.setPrefix(environment.getRequiredProperty("spring.thymeleaf.prefix.mail"));
		resolver.setSuffix(this.properties.getSuffix());
		resolver.setTemplateMode(this.properties.getMode());
		resolver.setCharacterEncoding(this.properties.getEncoding().name());
		resolver.setCacheable(this.properties.isCache());
		resolver.setOrder(1);
		return resolver;
	}

	@Bean(name = "emailTemplateEngine")
	public SpringTemplateEngine emailTemplateEngine() {
		SpringTemplateEngine engine = new SpringTemplateEngine();
		Set<ITemplateResolver> resolvers = new HashSet<>();
		resolvers.add(emailTemplateResolver());
		engine.setTemplateResolvers(resolvers);

		Set<IDialect> dialects = new HashSet<>();
		dialects.add(wallRideThymeleafDialect);
		dialects.add(new Java8TimeDialect());
		engine.setAdditionalDialects(dialects);
		return engine;
	}
}
