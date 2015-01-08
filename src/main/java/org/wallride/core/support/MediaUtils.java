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

package org.wallride.core.support;

import org.wallride.core.domain.Blog;
import org.wallride.core.domain.Media;
import org.wallride.core.service.BlogService;

public class MediaUtils {

	private BlogService blogService;

	public MediaUtils(BlogService blogService) {
		this.blogService = blogService;
	}

	public String link(Media media) {
		return link(media.getId());
	}

	public String link(String id) {
		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);
		return blog.getMediaUrlPrefix() + id;
	}
}
