package org.wallride.web.support;

import org.springframework.util.StringUtils;
import org.springframework.web.util.UrlPathHelper;
import org.wallride.core.domain.Setting;
import org.wallride.core.support.Settings;

import javax.servlet.http.HttpServletRequest;

public class LanguageUrlPathHelper extends UrlPathHelper {

	private Settings settings;

	public LanguageUrlPathHelper(Settings settings) {
		this.settings = settings;
	}

	@Override
	public String getLookupPathForRequest(HttpServletRequest request) {
		String defaultLanguage = settings.readSettingAsString(Setting.Key.DEFAULT_LANGUAGE);
		if (defaultLanguage != null) {
			String[] languages = StringUtils.commaDelimitedListToStringArray(settings.readSettingAsString(Setting.Key.LANGUAGES));
//			String[] languages = StringUtils.split(settingService.readSettingAsString(Setting.Key.LANGUAGES), ",");
			String path = super.getLookupPathForRequest(request);
			boolean languagePath = false;
			for (String language : languages) {
				if (path.startsWith("/" + language + "/")) {
					languagePath = true;
					break;
				}
			}
			if (!languagePath) {
				path = "/" + defaultLanguage + path;
			}
			return path;
		}
		else {
			return super.getLookupPathForRequest(request);
		}
	}
}
