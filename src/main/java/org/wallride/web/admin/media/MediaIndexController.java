package org.wallride.web.admin.media;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wallride.core.domain.Media;
import org.wallride.core.service.MediaService;
import org.wallride.core.support.Settings;

import javax.inject.Inject;
import java.util.List;

@Controller
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
