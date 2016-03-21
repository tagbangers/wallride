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

package org.wallride.web.controller.guest.page;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.util.UrlPathHelper;
import org.wallride.domain.Blog;
import org.wallride.domain.BlogLanguage;
import org.wallride.domain.Page;
import org.wallride.domain.Post;
import org.wallride.model.PageSearchRequest;
import org.wallride.service.BlogService;
import org.wallride.service.PageService;
import org.wallride.web.support.BlogLanguageMethodArgumentResolver;
import org.wallride.web.support.HttpNotFoundException;
import org.wallride.web.support.LanguageUrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public class PageDescribeController extends AbstractController {

	private static final String PATH_PATTERN = "/**/{code}";

	private BlogService blogService;
	private PageService pageService;
	private UrlPathHelper urlPathHelper;

	public PageDescribeController(BlogService blogService, PageService pageService) {
		this.blogService = blogService;
		this.pageService = pageService;
		this.urlPathHelper = new LanguageUrlPathHelper(blogService);
	}

	public BlogService getBlogService() {
		return blogService;
	}

	public PageService getPageService() {
		return pageService;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		BlogLanguage blogLanguage = (BlogLanguage) request.getAttribute(BlogLanguageMethodArgumentResolver.BLOG_LANGUAGE_ATTRIBUTE);
		if (blogLanguage == null) {
			Blog blog = blogService.getBlogById(Blog.DEFAULT_ID);
			blogLanguage = blog.getLanguage(blog.getDefaultLanguage());
		}

		String path = urlPathHelper.getLookupPathForRequest(request);

		PathMatcher pathMatcher = new AntPathMatcher();
		if (!pathMatcher.match(PATH_PATTERN, path)) {
			throw new HttpNotFoundException();
		}

		Map<String, String> variables = pathMatcher.extractUriTemplateVariables(PATH_PATTERN, path);
		request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, variables);

		Page page = pageService.getPageByCode(variables.get("code"), blogLanguage.getLanguage());
		if (page == null) {
			page = pageService.getPageByCode(variables.get("code"), blogLanguage.getBlog().getDefaultLanguage());
		}
		if (page == null) {
			throw new HttpNotFoundException();
		}
		if (page.getStatus() != Post.Status.PUBLISHED) {
			throw new HttpNotFoundException();
		}

		return createModelAndView(page);
	}

	protected ModelAndView createModelAndView(Page page) {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("page/describe");
		modelAndView.addObject("page", page);
		setBothSidesPages(modelAndView, page);
		return modelAndView;
	}

	protected ModelAndView setBothSidesPages(ModelAndView modelAndView, Page page) {
		List<Long> ids = pageService.getPageIds(new PageSearchRequest());
		if (!CollectionUtils.isEmpty(ids)) {
			int index = ids.indexOf(page.getId());
			if (index < ids.size() - 1) {
				Page next = pageService.getPageById(ids.get(index + 1));
				modelAndView.addObject("next", next);
			}
			if (index > 0) {
				Page prev = pageService.getPageById(ids.get(index - 1));
				modelAndView.addObject("prev", prev);
			}
		}
		return modelAndView;
	}
}
