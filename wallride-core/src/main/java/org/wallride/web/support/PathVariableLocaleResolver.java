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
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.LocaleResolver;
import org.wallride.core.domain.Blog;
import org.wallride.core.domain.BlogLanguage;
import org.wallride.core.service.BlogService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Map;

public class PathVariableLocaleResolver implements LocaleResolver {

	private BlogService blogService;

	private static Logger logger = LoggerFactory.getLogger(PathVariableLocaleResolver.class);

	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}

	@Override
	public Locale resolveLocale(HttpServletRequest request) {
		Blog blog = blogService.getBlogById(Blog.DEFAULT_ID);
		String defaultLanguage = (blog != null) ? blog.getDefaultLanguage() : null;

		Map<String, Object> pathVariables = (Map<String, Object>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
		String currentLanguage = (String) pathVariables.get("language");
		if (currentLanguage == null) {
			currentLanguage = defaultLanguage;
		}
		else {
			boolean correct = false;
			if (blog != null) {
				for (BlogLanguage blogLanguage : blog.getLanguages()) {
					if (blogLanguage.getLanguage().equals(currentLanguage)) {
						correct = true;
						break;
					}
				}
			}
			if (!correct) {
				currentLanguage = defaultLanguage;
			}
		}

		return (currentLanguage != null) ? Locale.forLanguageTag(currentLanguage) : request.getLocale();
	}

	@Override
	public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
		throw new UnsupportedOperationException(
				"Cannot change fixed locale - use a different locale resolution strategy");
	}
}
