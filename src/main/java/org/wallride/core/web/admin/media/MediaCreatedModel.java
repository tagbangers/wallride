package org.wallride.core.web.admin.media;

import org.wallride.core.domain.Media;
import org.wallride.core.domain.Setting;
import org.wallride.core.support.Settings;

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
