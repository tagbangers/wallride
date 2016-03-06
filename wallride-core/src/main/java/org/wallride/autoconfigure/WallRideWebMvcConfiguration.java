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
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.support.RequestDataValueProcessor;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.wallride.service.BlogService;
import org.wallride.web.support.AtomFeedView;
import org.wallride.web.support.BlogLanguageDataValueProcessor;
import org.wallride.web.support.RssFeedView;

@Configuration
public class WallRideWebMvcConfiguration  {

	@Autowired
	private BlogService blogService;

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
