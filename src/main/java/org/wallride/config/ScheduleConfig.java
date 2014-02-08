package org.wallride.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.wallride.core.service.PostService;

import javax.inject.Inject;

@Configuration
@EnableScheduling
public class ScheduleConfig {

	@Inject
	private PostService postService;

	@Scheduled(cron="0 0 * * * *")
	public void publishPost() {
		postService.publishScheduledPosts();
	}
}
