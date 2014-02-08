package org.wallride.web.admin.media;

import org.wallride.domain.Media;
import org.wallride.domain.Setting;
import org.wallride.support.Settings;

import java.io.Serializable;

public class MediaCreatedModel implements Serializable {

	private String id;

	private String filelink;

	public MediaCreatedModel(Media media, Settings settings) {
		this.id = media.getId();
		this.filelink = settings.readSettingAsString(Setting.Key.MEDIA_URL_PREFIX) + media.getId();
	}

	public String getId() {
		return id;
	}

	public String getFilelink() {
		return filelink;
	}
}
