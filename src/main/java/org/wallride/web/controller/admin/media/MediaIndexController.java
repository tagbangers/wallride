package org.wallride.web.controller.admin.media;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wallride.core.domain.Blog;
import org.wallride.core.domain.Media;
import org.wallride.core.service.BlogService;
import org.wallride.core.service.MediaService;
import org.wallride.core.support.Settings;

import javax.inject.Inject;
import java.util.List;

@Controller
@RequestMapping("/{language}/media/index")
public class MediaIndexController {

	@Inject
	private BlogService blogService;
	@Inject
	private MediaService mediaService;

	@RequestMapping
	public @ResponseBody MediaIndexModel[] index() {
		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);

		List<Media> medias = mediaService.readAllMedias();
		MediaIndexModel[] models = new MediaIndexModel[medias.size()];
		for (int i = 0; i < medias.size(); i++) {
			models[i] = new MediaIndexModel(medias.get(i), blog);
		}
		return models;
	}
}
