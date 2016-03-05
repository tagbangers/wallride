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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import org.springframework.web.servlet.support.RequestDataValueProcessor;
import org.wallride.repository.MediaRepository;
import org.wallride.service.BlogService;
import org.wallride.web.support.BlogLanguageDataValueProcessor;
import org.wallride.web.support.BlogLanguageLocaleResolver;
import org.wallride.web.support.MediaHttpRequestHandler;

import java.util.*;

@Configuration
public class WebGuestConfiguration  {

	private static final String CLASSPATH_RESOURCE_LOCATION = "classpath:/resources/guest/";

	@Autowired
	private BlogService blogService;

	@Autowired
	private MediaRepository mediaRepository;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private WallRideProperties wallRideProperties;

	@Bean
	public SimpleUrlHandlerMapping faviconHandlerMapping() {
		ResourceHttpRequestHandler requestHandler = new ResourceHttpRequestHandler();
		List<org.springframework.core.io.Resource> locations = new ArrayList<>();
		locations.add(resourceLoader.getResource(wallRideProperties.getHome() + "themes/default/resources/"));
		locations.add(resourceLoader.getResource(CLASSPATH_RESOURCE_LOCATION));
		requestHandler.setLocations(locations);

		SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
		mapping.setOrder(Integer.MIN_VALUE + 1);
		mapping.setUrlMap(Collections.singletonMap("**/favicon.ico", requestHandler));
		return mapping;
	}

	@Bean
	public SimpleUrlHandlerMapping mediaUrlHandlerMapping() {
		MediaHttpRequestHandler mediaHttpRequestHandler = new MediaHttpRequestHandler();
		mediaHttpRequestHandler.setWallRideProperties(wallRideProperties);
//		mediaHttpRequestHandler.setBlogService(blogService);
		mediaHttpRequestHandler.setMediaRepository(mediaRepository);
		mediaHttpRequestHandler.setResourceLoader(resourceLoader);
		mediaHttpRequestHandler.setCacheSeconds(86400);

		Map<String, HttpRequestHandler> urlMap = new LinkedHashMap<>();
		urlMap.put("/media/{key}", mediaHttpRequestHandler);

		SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
		handlerMapping.setOrder(0);
		handlerMapping.setUrlMap(urlMap);
		return handlerMapping;
	}

	@Bean
	public LocaleResolver localeResolver() {
		BlogLanguageLocaleResolver blogLanguageLocaleResolver = new BlogLanguageLocaleResolver();
		blogLanguageLocaleResolver.setBlogService(blogService);
		return blogLanguageLocaleResolver;
	}
}
