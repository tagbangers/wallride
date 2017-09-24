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

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.WebMvcProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.format.FormatterRegistry;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.support.RequestDataValueProcessor;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.wallride.service.BlogService;
import org.wallride.service.MediaService;
import org.wallride.support.CodeFormatAnnotationFormatterFactory;
import org.wallride.support.StringFormatter;
import org.wallride.web.support.*;

import javax.servlet.ServletContext;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties({ WebMvcProperties.class, ResourceProperties.class })
public class WallRideWebMvcConfiguration extends WebMvcConfigurerAdapter {

	private static final String CLASSPATH_RESOURCE_LOCATION = "classpath:/resources/guest/";

	@Autowired
	private WallRideProperties wallRideProperties;

	@Autowired
	private ResourceProperties resourceProperties = new ResourceProperties();

	@Autowired
	private MessageCodesResolver messageCodesResolver;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private BlogService blogService;

	@Autowired
	private MediaService mediaService;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		Integer cachePeriod = this.resourceProperties.getCachePeriod();
		registry.addResourceHandler("/resources/**").addResourceLocations(wallRideProperties.getHome() + "themes/default/resources/", CLASSPATH_RESOURCE_LOCATION)
				.setCachePeriod(cachePeriod);
		registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/")
				.setCachePeriod(cachePeriod);
		registry.setOrder(Integer.MIN_VALUE);
	}

	@Override
	public void addFormatters(FormatterRegistry registry) {
		super.addFormatters(registry);
		registry.addFormatterForFieldAnnotation(new CodeFormatAnnotationFormatterFactory());
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		BlogLanguageMethodArgumentResolver blogLanguageMethodArgumentResolver = new BlogLanguageMethodArgumentResolver();
		blogLanguageMethodArgumentResolver.setBlogService(blogService);

		argumentResolvers.add(new PageableHandlerMethodArgumentResolver());
		argumentResolvers.add(new AuthorizedUserMethodArgumentResolver());
		argumentResolvers.add(blogLanguageMethodArgumentResolver);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(defaultModelAttributeInterceptor());
		registry.addInterceptor(setupRedirectInterceptor());
	}

	@Override
	public MessageCodesResolver getMessageCodesResolver() {
		return messageCodesResolver;
	}

	// additional webmvc-related beans

	@Bean
	public SimpleUrlHandlerMapping mediaUrlHandlerMapping(
			ApplicationContext applicationContext,
			ServletContext servletContext,
			ContentNegotiationManager contentNegotiationManager) {
		MediaHttpRequestHandler handler = new MediaHttpRequestHandler();

		handler.setServletContext(servletContext);
		handler.setApplicationContext(applicationContext);
		handler.setContentNegotiationManager(contentNegotiationManager);

		handler.setWallRideProperties(wallRideProperties);
		handler.setMediaService(mediaService);
		handler.setResourceLoader(resourceLoader);
		handler.setCacheSeconds(86400);

		try {
			handler.afterPropertiesSet();
		} catch (Exception e) {
			throw new BeanInitializationException("Failed to init MediaHttpRequestHandler", e);
		}

		Map<String, HttpRequestHandler> urlMap = new LinkedHashMap<>();
		urlMap.put("/media/{key}", handler);

		SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
		handlerMapping.setOrder(0);
		handlerMapping.setUrlMap(urlMap);
		return handlerMapping;
	}

	@Bean
	public StringFormatter stringFormatter() {
		return new StringFormatter();
	}

	@Bean
	public DefaultModelAttributeInterceptor defaultModelAttributeInterceptor() {
		DefaultModelAttributeInterceptor defaultModelAttributeInterceptor = new DefaultModelAttributeInterceptor();
		defaultModelAttributeInterceptor.setBlogService(blogService);
		return defaultModelAttributeInterceptor;
	}

	@Bean
	public SetupRedirectInterceptor setupRedirectInterceptor() {
		SetupRedirectInterceptor setupRedirectInterceptor = new SetupRedirectInterceptor();
		setupRedirectInterceptor.setBlogService(blogService);
		return setupRedirectInterceptor;
	}

	@Bean
	public LocaleResolver localeResolver() {
		BlogLanguageLocaleResolver blogLanguageLocaleResolver = new BlogLanguageLocaleResolver();
		blogLanguageLocaleResolver.setBlogService(blogService);
		return blogLanguageLocaleResolver;
	}

	@Bean
	public MultipartResolver multipartResolver() {
		return new CommonsMultipartResolver();
	}

	@Bean
	public RequestDataValueProcessor requestDataValueProcessor() {
		return new BlogLanguageDataValueProcessor();
	}

	@Bean(name = {"viewResolver", "beanNameViewResolver"})
	public BeanNameViewResolver beanNameViewResolver() {
		BeanNameViewResolver viewResolver = new BeanNameViewResolver();
		viewResolver.setOrder(1);
		return viewResolver;
	}

	@Bean(name = "atomFeedView")
	public View atomFeedView() {
		AtomFeedView view = new AtomFeedView();
		view.setBlogService(blogService);
		return view;
	}

	@Bean(name = "rssFeedView")
	public View rssFeedView() {
		RssFeedView view = new RssFeedView();
		view.setBlogService(blogService);
		return view;
	}
}
