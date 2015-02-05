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

package org.wallride.web.controller.admin.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.wallride.core.service.ServiceException;
import org.wallride.core.service.SystemService;
import org.wallride.web.support.DefaultModelAttributeInterceptor;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/{language}/system")
public class SystemIndexController {

	private static Logger logger = LoggerFactory.getLogger(SystemIndexController.class);

	@Inject
	private SystemService systemService;

	@Inject
	private ServletContext servletContext;

	@RequestMapping(method = RequestMethod.GET)
	public String index(Model model) {
		model.addAttribute("system", System.getProperties());
		return "system/index";
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@RequestMapping(value = "/re-index", method = RequestMethod.POST)
	public String reIndex(
			@PathVariable String language,
			RedirectAttributes redirectAttributes) throws Exception {
//		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
//		fullTextEntityManager.createIndexer().start();

		systemService.reIndex();

		redirectAttributes.addFlashAttribute("reIndex", true);
		redirectAttributes.addAttribute("language", language);
		return "redirect:/_admin/{language}/system";
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@RequestMapping(value = "/clear-cache", method = RequestMethod.POST)
	public String clearCache(
			@PathVariable String language,
			RedirectAttributes redirectAttributes,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(servletContext, "org.springframework.web.servlet.FrameworkServlet.CONTEXT.guestServlet");
		if (context == null) {
			throw new ServiceException("GuestServlet is not ready yet");
		}

		DefaultModelAttributeInterceptor interceptor = context.getBean(DefaultModelAttributeInterceptor.class);
		ModelAndView mv = new ModelAndView("dummy");
		interceptor.postHandle(request, response, this, mv);

		SpringTemplateEngine templateEngine = context.getBean(SpringTemplateEngine.class);
		logger.info("Clear cache started");
		templateEngine.clearTemplateCache();
		logger.info("Clear cache finished");

		redirectAttributes.addFlashAttribute("clearCache", true);
		redirectAttributes.addAttribute("language", language);
		return "redirect:/_admin/{language}/system";
	}
}
