package org.wallride.admin.web.media;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.wallride.admin.service.MediaService;
import org.wallride.core.domain.Media;

import javax.inject.Inject;

@Controller
@RequestMapping("/{language}/media/create")
public class MediaCreateController {

	@Inject
	private MediaService mediaService;

	@Inject
	private Environment environment;

	@RequestMapping(method=RequestMethod.POST)
	public @ResponseBody MediaCreatedModel create(@RequestParam MultipartFile file) {
		Media media = mediaService.createMedia(file);
		return new MediaCreatedModel(media, environment);
	}
}
