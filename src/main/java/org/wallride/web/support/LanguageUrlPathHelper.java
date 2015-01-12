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

import org.springframework.web.util.UrlPathHelper;
import org.wallride.core.domain.Blog;
import org.wallride.core.domain.BlogLanguage;
import org.wallride.core.service.BlogService;

import javax.servlet.http.HttpServletRequest;

public class LanguageUrlPathHelper extends UrlPathHelper {

	private BlogService blogService;

	public LanguageUrlPathHelper(BlogService blogService) {
		this.blogService = blogService;
	}

	@Override
	public String getLookupPathForRequest(HttpServletRequest request) {
		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);
		String defaultLanguage = (blog != null) ? blog.getDefaultLanguage() : null;
		if (defaultLanguage != null) {
			String path = super.getLookupPathForRequest(request);
			boolean languagePath = false;
			for (BlogLanguage blogLanguage : blog.getLanguages()) {
				if (path.startsWith("/" + blogLanguage.getLanguage() + "/")) {
					languagePath = true;
					break;
				}
			}
			if (!languagePath) {
				path = "/" + defaultLanguage + path;
			}
			return path;
		}
		else {
			return super.getLookupPathForRequest(request);
		}
	}
}
