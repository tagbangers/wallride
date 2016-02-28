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

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.wallride.core.domain.Blog;
import org.wallride.core.domain.BlogLanguage;
import org.wallride.core.domain.PopularPost;
import org.wallride.core.service.BlogService;
import org.wallride.core.service.PostService;

import javax.inject.Inject;

@Configuration
@EnableScheduling
public class ScheduleConfiguration {

	private static final int POPULAR_POST_MAX_RANK = 5;

	@Inject
	private BlogService blogService;
	@Inject
	private PostService postService;

	@Scheduled(cron="0 0 * * * *")
	public void publishPost() {
		postService.publishScheduledPosts();
	}

	@Scheduled(cron="0 0 3,15 * * *")
	public void updatePostViews() {
		postService.updatePostViews();
	}

	@Scheduled(cron="0 0 4,16 * * *")
	public void updatePopularPosts() {
		Blog blog = blogService.getBlogById(Blog.DEFAULT_ID);
		for (BlogLanguage blogLanguage : blog.getLanguages()) {
			for (PopularPost.Type type : PopularPost.Type.values()) {
				postService.updatePopularPosts(blogLanguage, type, POPULAR_POST_MAX_RANK);
			}
		}
	}
}
