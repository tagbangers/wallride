package org.wallride.web.controller.admin.media;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.wallride.core.domain.Blog;
import org.wallride.core.domain.Media;
import org.wallride.core.service.BlogService;
import org.wallride.core.service.MediaService;

import javax.inject.Inject;

@Controller
@RequestMapping("/media/create")
public class MediaCreateController {

	@Inject
	private BlogService blogService;
	@Inject
	private MediaService mediaService;

	@RequestMapping(method=RequestMethod.POST)
	public @ResponseBody MediaCreatedModel create(@RequestParam MultipartFile file) {
		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);

		Media media = mediaService.createMedia(file);
		return new MediaCreatedModel(media, blog);
	}
}
