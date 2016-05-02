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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.WebContentInterceptor;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.extras.springsecurity4.dialect.SpringSecurityDialect;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.wallride.service.BlogService;
import org.wallride.support.CodeFormatAnnotationFormatterFactory;
import org.wallride.web.controller.admin.DashboardController;
import org.wallride.web.support.*;

import java.text.DateFormat;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Configuration
@ComponentScan(basePackageClasses = DashboardController.class)
public class WebAdminConfiguration extends WebMvcConfigurationSupport {

	@Autowired
	private MessageCodesResolver messageCodesResolver;

	@Autowired
	private WallRideThymeleafDialect wallRideThymeleafDialect;

	@Autowired
	private BlogService blogService;

	@Autowired
	private DefaultModelAttributeInterceptor defaultModelAttributeInterceptor;

	@Autowired
	private SetupRedirectInterceptor setupRedirectInterceptor;

	@Autowired
	private Environment environment;

	@Autowired
	private ThymeleafProperties properties;

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
//		converters.add(new FormHttpMessageConverter());

		converters.add(new ByteArrayHttpMessageConverter());
		converters.add(new ResourceHttpMessageConverter());

		MappingJackson2HttpMessageConverter jackson = new MappingJackson2HttpMessageConverter();
		ObjectMapper objectMapper = new ObjectMapper();
//		objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ");
		objectMapper.setDateFormat(dateFormat);
		jackson.setObjectMapper(objectMapper);
		converters.add(jackson);
	}

//	@Override
//	public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
//		exceptionResolvers.add(new ExceptionHandlerExceptionResolver());
//	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/resources/**").addResourceLocations("classpath:/resources/admin/");
		registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
		registry.setOrder(Integer.MIN_VALUE);
	}

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addFormatter(new Formatter<String>() {
			@Override
			public String print(String object, Locale locale) {
				return (!object.equals("") ? object : null);
			}

			@Override
			public String parse(String text, Locale locale) throws ParseException {
				String value = StringUtils.trimWhitespace(text);
				return Normalizer.normalize(value, Normalizer.Form.NFKC);
			}
		});
		registry.addFormatterForFieldAnnotation(new CodeFormatAnnotationFormatterFactory());
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(new PageableHandlerMethodArgumentResolver());
		argumentResolvers.add(new AuthorizedUserMethodArgumentResolver());
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		WebContentInterceptor webContentInterceptor = new WebContentInterceptor();
		webContentInterceptor.setCacheSeconds(0);
		webContentInterceptor.setUseExpiresHeader(true);
		webContentInterceptor.setUseCacheControlHeader(true);
		webContentInterceptor.setUseCacheControlNoStore(true);
		registry.addInterceptor(webContentInterceptor);

		registry.addInterceptor(defaultModelAttributeInterceptor);
		registry.addInterceptor(setupRedirectInterceptor);
	}

	@Override
	public MessageCodesResolver getMessageCodesResolver() {
		return messageCodesResolver;
	}

	// additional webmvc-related beans

	@Bean(name = "adminTemplateResolver")
	public ITemplateResolver adminTemplateResolver() {
		WallRideResourceTemplateResolver resolver = new WallRideResourceTemplateResolver();
//		resolver.setResourceResolver(wallRideResourceResourceResolver);
		resolver.setApplicationContext(getApplicationContext());
		resolver.setPrefix(environment.getRequiredProperty("spring.thymeleaf.prefix.admin"));
		resolver.setSuffix(properties.getSuffix());
		resolver.setTemplateMode(properties.getMode());
		resolver.setCharacterEncoding(properties.getEncoding().name());
		resolver.setCacheable(properties.isCache());
		resolver.setOrder(2);
		return resolver;
	}

	@Bean(name = "guestTemplateResolver")
	public ITemplateResolver guestTemplateResolver() {
		WallRideResourceTemplateResolver resolver = new WallRideResourceTemplateResolver();
//		resolver.setResourceResolver(wallRideResourceResourceResolver);
		resolver.setApplicationContext(getApplicationContext());
		resolver.setPrefix(environment.getRequiredProperty("spring.thymeleaf.prefix.guest"));
		resolver.setSuffix(this.properties.getSuffix());
		resolver.setTemplateMode(this.properties.getMode());
		resolver.setCharacterEncoding(this.properties.getEncoding().name());
		resolver.setCacheable(this.properties.isCache());
		resolver.setOrder(2);
		return resolver;
	}

	@Bean
	public SpringTemplateEngine templateEngine() {
		SpringTemplateEngine engine = new SpringTemplateEngine();
		Set<ITemplateResolver> resolvers = new HashSet<>();
		resolvers.add(adminTemplateResolver());
		engine.setTemplateResolvers(resolvers);

		Set<IDialect> dialects = new HashSet<>();
		dialects.add(new SpringSecurityDialect());
		dialects.add(new Java8TimeDialect());
		dialects.add(wallRideThymeleafDialect);
		engine.setAdditionalDialects(dialects);
		return engine;
	}

	@Bean
	public ThymeleafViewResolver thymeleafViewResolver() {
		ThymeleafViewResolver viewResolver = new ExtendedThymeleafViewResolver();
		viewResolver.setTemplateEngine(templateEngine());
		viewResolver.setViewNames(this.properties.getViewNames());
		viewResolver.setCharacterEncoding(this.properties.getEncoding().name());
		viewResolver.setContentType(this.properties.getContentType() + ";charset=" + this.properties.getEncoding());
		viewResolver.setCache(false);
		viewResolver.setOrder(1);
		return viewResolver;
	}

	@Bean
	public MultipartResolver multipartResolver() {
		return new CommonsMultipartResolver();
	}

	@Bean
	public LocaleResolver localeResolver() {
		PathVariableLocaleResolver pathVariableLocaleResolver = new PathVariableLocaleResolver();
		pathVariableLocaleResolver.setBlogService(blogService);
		return pathVariableLocaleResolver;
	}
}
