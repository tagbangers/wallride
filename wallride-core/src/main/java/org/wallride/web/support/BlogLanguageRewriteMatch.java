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
import org.springframework.web.util.UrlPathHelper;
import org.tuckey.web.filters.urlrewrite.extend.RewriteMatch;
import org.wallride.core.domain.BlogLanguage;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class BlogLanguageRewriteMatch extends RewriteMatch {

	private BlogLanguage blogLanguage;
	private String matchingUrl;

	private static Logger logger = LoggerFactory.getLogger(BlogLanguageRewriteMatch.class);

	public BlogLanguageRewriteMatch(BlogLanguage blogLanguage) {
		this.blogLanguage = blogLanguage;
	}

	public BlogLanguage getBlogLanguage() {
		return blogLanguage;
	}

	@Override
	public String getMatchingUrl() {
		return matchingUrl;
	}

	@Override
	public boolean execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		UrlPathHelper urlPathHelper = new UrlPathHelper();
		String originalPath = urlPathHelper.getLookupPathForRequest(request);

		String rewritePath = originalPath.replaceAll("^/" + blogLanguage.getLanguage() + "/", "/");
		matchingUrl = rewritePath;
		logger.debug(originalPath + " => " + rewritePath);

		request.setAttribute(BlogLanguageMethodArgumentResolver.BLOG_LANGUAGE_ATTRIBUTE, blogLanguage);

		RequestDispatcher rd = request.getRequestDispatcher(urlPathHelper.getServletPath(request) + rewritePath);
		rd.forward(request, response);
		return true;
	}
}
