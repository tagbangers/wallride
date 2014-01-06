package org.wallride.admin.web.media;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wallride.admin.service.MediaService;
import org.wallride.core.domain.Media;

import javax.inject.Inject;
import java.util.List;

@Controller
@RequestMapping("/{language}/media/index")
public class MediaIndexController {

	@Inject
	private MediaService mediaService;

	@Inject
	private Environment environment;

	@RequestMapping
	public @ResponseBody MediaIndexModel[] index() {
		List<Media> medias = mediaService.readAllMedias();
		MediaIndexModel[] models = new MediaIndexModel[medias.size()];
		for (int i = 0; i < medias.size(); i++) {
			models[i] = new MediaIndexModel(medias.get(i), environment);
		}
		return models;
	}
}
