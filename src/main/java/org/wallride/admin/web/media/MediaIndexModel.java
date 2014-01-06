package org.wallride.admin.web.media;

import org.springframework.core.env.Environment;
import org.wallride.core.domain.Media;

import java.io.Serializable;

public class MediaIndexModel implements Serializable {

	private String thumb;

	private String image;

	private String title;

	private String folder;

	public MediaIndexModel(Media media, Environment environment) {
		this.thumb = environment.getRequiredProperty("media.default.url") + media.getId() + "?w=100&h=100&m=1";
		this.image = environment.getRequiredProperty("media.default.url") + media.getId();
		this.title = media.getOriginalName();
		this.folder = media.getCreatedAt().toString("yyyy/MM");
	}

	public String getThumb() {
		return thumb;
	}

	public String getImage() {
		return image;
	}

	public String getTitle() {
		return title;
	}

	public String getFolder() {
		return folder;
	}
}
