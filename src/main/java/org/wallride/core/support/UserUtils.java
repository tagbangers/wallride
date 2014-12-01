package org.wallride.core.support;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.context.IProcessingContext;
import org.wallride.core.domain.Blog;
import org.wallride.core.domain.User;
import org.wallride.core.service.BlogService;

import java.util.HashMap;
import java.util.Map;

public class UserUtils {

	private IProcessingContext processingContext;
	private BlogService blogService;

	public UserUtils(IProcessingContext processingContext, BlogService blogService) {
		this.processingContext = processingContext;
		this.blogService = blogService;
	}

	public String link(User user) {
		UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath();
		return path(builder, user, true);
	}

	public String link(User user, boolean encode) {
		UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath();
		return path(builder, user, encode);
	}

	public String path(User user) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("");
		return path(builder, user, true);
	}

	public String path(User user, boolean encode) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("");
		return path(builder, user, encode);
	}

	private String path(UriComponentsBuilder builder, User user, boolean encode) {
		Map<String, Object> params = new HashMap<>();
		builder.path("/author/{code}");
		params.put("code", user.getLoginId());

		UriComponents components = builder.buildAndExpand(params);
		if (encode) {
			components = components.encode();
		}
		return components.toUriString();
	}

	public String title(User user) {
		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);
		return String.format("%s | %s",
				user.getNickname(),
				blog.getTitle(processingContext.getContext().getLocale().getLanguage()));
	}
}
