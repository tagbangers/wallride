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
