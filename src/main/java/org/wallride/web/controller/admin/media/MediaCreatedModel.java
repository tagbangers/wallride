package org.wallride.web.controller.admin.media;

import org.wallride.core.domain.Blog;
import org.wallride.core.domain.Media;
import org.wallride.core.domain.Setting;
import org.wallride.core.support.Settings;

import java.io.Serializable;

public class MediaCreatedModel implements Serializable {

	private String id;

	private String filelink;

	public MediaCreatedModel(Media media, Blog blog) {
		this.id = media.getId();
		this.filelink = blog.getMediaUrlPrefix() + media.getId();
	}

	public String getId() {
		return id;
	}

	public String getFilelink() {
		return filelink;
	}
}
