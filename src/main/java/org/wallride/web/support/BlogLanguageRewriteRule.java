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

import org.springframework.util.ObjectUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.UrlPathHelper;
import org.tuckey.web.filters.urlrewrite.extend.RewriteMatch;
import org.tuckey.web.filters.urlrewrite.extend.RewriteRule;
import org.wallride.Application;
import org.wallride.core.domain.Blog;
import org.wallride.core.domain.BlogLanguage;
import org.wallride.core.service.BlogService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BlogLanguageRewriteRule extends RewriteRule {

//	private BlogService blogService;
//
//	@Override
//	public boolean initialise(ServletContext servletContext) {
//		WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(servletContext);
//		blogService = context.getBean(BlogService.class);
//		return super.initialise(servletContext);
//	}

	@Override
	public RewriteMatch matches(HttpServletRequest request, HttpServletResponse response) {
		UrlPathHelper urlPathHelper = new UrlPathHelper();

		String servletPath = urlPathHelper.getOriginatingServletPath(request);
		if (ObjectUtils.nullSafeEquals(servletPath, Application.ADMIN_SERVLET_PATH)) {
			return null;
		}

		String lookupPath = urlPathHelper.getLookupPathForRequest(request);

		WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());
		BlogService blogService = context.getBean(BlogService.class);
		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);

		BlogLanguage matchedBlogLanguage = null;
		for (BlogLanguage blogLanguage : blog.getLanguages()) {
			if (lookupPath.startsWith("/" + blogLanguage.getLanguage() + "/")) {
				matchedBlogLanguage = blogLanguage;
				break;
			}
		}

		if (matchedBlogLanguage == null) {
			matchedBlogLanguage = blog.getLanguage(blog.getDefaultLanguage());
		}

		return new BlogLanguageRewriteMatch(matchedBlogLanguage);
	}
}
