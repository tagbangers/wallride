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

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.UrlPathHelper;
import org.wallride.core.domain.Blog;
import org.wallride.core.service.BlogService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SetupRedirectInterceptor extends HandlerInterceptorAdapter {

    private static final String SETUP_PATH = "/_admin/setup";

	private BlogService blogService;

	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}

	@Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);
		if (blog != null) {
            return true;
        }

        final String requestPath = getRequestPath(request);
        if (!SETUP_PATH.equalsIgnoreCase(requestPath)) {
            response.sendRedirect(request.getContextPath() + SETUP_PATH);
            return false;
        }

        return true;
    }

    private String getRequestPath(HttpServletRequest request) {
		UrlPathHelper urlPathHelper = new UrlPathHelper();
		return urlPathHelper.getPathWithinApplication(request);
//        return (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    }
}
