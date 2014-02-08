package org.wallride.support;

import org.wallride.domain.Media;
import org.wallride.domain.Setting;

//@Component
public class MediaUtils {

//	private Environment environment;

	private Settings settings;

	public MediaUtils(Settings settings) {
		this.settings = settings;
	}

	public String link(Media media) {
		return link(media.getId());
	}

	public String link(String id) {
		return settings.readSettingAsString(Setting.Key.MEDIA_URL_PREFIX) + id;
	}
}
