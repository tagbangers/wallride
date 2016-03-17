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
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.wallride.service.BlogService;
import org.wallride.service.PageService;
import org.wallride.web.controller.guest.IndexController;
import org.wallride.web.controller.guest.page.PageDescribeController;
import org.wallride.web.support.DefaultModelAttributeInterceptor;
import org.wallride.web.support.SetupRedirectInterceptor;

@Configuration
@ComponentScan(basePackageClasses = IndexController.class)
public class WebGuestConfiguration {

	@Autowired
	private BlogService blogService;

	@Autowired
	private PageService pageService;

	@Autowired
	private ContentNegotiationManager mvcContentNegotiationManager;

	@Autowired
	private DefaultModelAttributeInterceptor defaultModelAttributeInterceptor;

	@Autowired
	private SetupRedirectInterceptor setupRedirectInterceptor;

	@Bean(name = "guestRequestMappingHandlerMapping")
	public RequestMappingHandlerMapping requestMappingHandlerMapping(PageDescribeController pageDescribeController) {
		RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping();
		handlerMapping.setOrder(Ordered.LOWEST_PRECEDENCE);
		handlerMapping.setDefaultHandler(pageDescribeController);
		handlerMapping.setInterceptors(new HandlerInterceptor[] {
				defaultModelAttributeInterceptor,
				setupRedirectInterceptor
		});
		handlerMapping.setContentNegotiationManager(mvcContentNegotiationManager);
		return handlerMapping;
	}

	@Bean
	@ConditionalOnMissingBean
	public PageDescribeController pageDescribeController() {
		return new PageDescribeController(blogService, pageService);
	}
}
