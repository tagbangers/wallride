package org.wallride.web.controller.admin.media;

import org.wallride.core.domain.Media;
import org.wallride.core.domain.Setting;
import org.wallride.core.support.Settings;

import java.io.Serializable;

public class MediaIndexModel implements Serializable {

	private String thumb;

	private String image;

	private String title;

	private String folder;

	public MediaIndexModel(Media media, Settings settings) {
		this.thumb = settings.readSettingAsString(Setting.Key.MEDIA_URL_PREFIX) + media.getId() + "?w=100&h=100&m=1";
		this.image = settings.readSettingAsString(Setting.Key.MEDIA_URL_PREFIX) + media.getId();
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
