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

import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.resourceresolver.SpringResourceResourceResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class WallRideMailConfiguration extends MailSenderAutoConfiguration {

	@Inject
	private SpringResourceResourceResolver springResourceResourceResolver;

	@Inject
	private WallRideThymeleafDialect wallRideThymeleafDialect;

	@Inject
	private Environment environment;

	@Inject
	private ThymeleafProperties properties;

	@Bean(name = "emailTemplateResolver")
	public TemplateResolver emailTemplateResolver() {
		TemplateResolver resolver = new TemplateResolver();
		resolver.setResourceResolver(springResourceResourceResolver);
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
		Set<TemplateResolver> resolvers = new HashSet<>();
		resolvers.add(emailTemplateResolver());
		engine.setTemplateResolvers(resolvers);

		Set<IDialect> dialects = new HashSet<>();
		dialects.add(wallRideThymeleafDialect);
		engine.setAdditionalDialects(dialects);
		return engine;
	}
}
