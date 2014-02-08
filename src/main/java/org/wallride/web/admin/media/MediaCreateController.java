package org.wallride.web.admin.media;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.wallride.service.MediaService;
import org.wallride.domain.Media;
import org.wallride.support.Settings;

import javax.inject.Inject;

@Controller @Lazy
@RequestMapping("/{language}/media/create")
public class MediaCreateController {

	@Inject
	private MediaService mediaService;

	@Inject
	private Settings settings;

	@RequestMapping(method=RequestMethod.POST)
	public @ResponseBody MediaCreatedModel create(@RequestParam MultipartFile file) {
		Media media = mediaService.createMedia(file);
		return new MediaCreatedModel(media, settings);
	}
}
