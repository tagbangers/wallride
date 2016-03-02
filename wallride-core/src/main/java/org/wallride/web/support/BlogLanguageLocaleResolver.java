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

package org.wallride.web.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.LocaleResolver;
import org.wallride.domain.Blog;
import org.wallride.domain.BlogLanguage;
import org.wallride.service.BlogService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

public class BlogLanguageLocaleResolver implements LocaleResolver {

	private BlogService blogService;

	private static Logger logger = LoggerFactory.getLogger(BlogLanguageLocaleResolver.class);

	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}

	@Override
	public Locale resolveLocale(HttpServletRequest request) {
		BlogLanguage blogLanguage = (BlogLanguage) request.getAttribute(BlogLanguageMethodArgumentResolver.BLOG_LANGUAGE_ATTRIBUTE);
		if (blogLanguage == null) {
			Blog blog = blogService.getBlogById(Blog.DEFAULT_ID);
			blogLanguage = blog.getLanguage(blog.getDefaultLanguage());
		}

		return (blogLanguage != null) ? Locale.forLanguageTag(blogLanguage.getLanguage()) : request.getLocale();
	}

	@Override
	public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
		throw new UnsupportedOperationException(
				"Cannot change fixed locale - use a different locale resolution strategy");
	}
}
