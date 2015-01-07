package org.wallride.config;

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
public class ScheduleConfig {

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
		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);
		for (BlogLanguage blogLanguage : blog.getLanguages()) {
			for (PopularPost.Type type : PopularPost.Type.values()) {
				postService.updatePopularPosts(blogLanguage, type, POPULAR_POST_MAX_RANK);
			}
		}
	}
}
