package org.wallride.admin.web.media;

import org.springframework.core.env.Environment;
import org.wallride.core.domain.Media;

import java.io.Serializable;

public class MediaCreatedModel implements Serializable {

	private String id;

	private String filelink;

	public MediaCreatedModel(Media media, Environment environment) {
		this.id = media.getId();
		this.filelink = environment.getRequiredProperty("media.default.url") + media.getId();
	}

	public String getId() {
		return id;
	}

	public String getFilelink() {
		return filelink;
	}
}
