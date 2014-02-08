package org.wallride.web.admin.media;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wallride.service.MediaService;
import org.wallride.domain.Media;
import org.wallride.support.Settings;

import javax.inject.Inject;
import java.util.List;

@Controller @Lazy
@RequestMapping("/{language}/media/index")
public class MediaIndexController {

	@Inject
	private MediaService mediaService;

	@Inject
	private Settings settings;

	@RequestMapping
	public @ResponseBody MediaIndexModel[] index() {
		List<Media> medias = mediaService.readAllMedias();
		MediaIndexModel[] models = new MediaIndexModel[medias.size()];
		for (int i = 0; i < medias.size(); i++) {
			models[i] = new MediaIndexModel(medias.get(i), settings);
		}
		return models;
	}
}
