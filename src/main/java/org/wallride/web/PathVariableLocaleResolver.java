package org.wallride.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.LocaleResolver;
import org.wallride.core.domain.Setting;
import org.wallride.core.support.Settings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Map;

public class PathVariableLocaleResolver implements LocaleResolver {

	private Settings settings;

	private static Logger logger = LoggerFactory.getLogger(PathVariableLocaleResolver.class);

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	@Override
	public Locale resolveLocale(HttpServletRequest request) {
		String defaultLanguage = settings.readSettingAsString(Setting.Key.DEFAULT_LANGUAGE);

		Map<String, Object> pathVariables = (Map<String, Object>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
		String currentLanguage = (String) pathVariables.get("language");
		if (currentLanguage == null) {
			currentLanguage = defaultLanguage;
		}
		else {
			String[] languages = settings.readSettingAsStringArray(Setting.Key.LANGUAGES, ",");
			boolean correct = false;
			for (String language : languages) {
				if (language.equals(currentLanguage)) {
					correct = true;
					break;
				}
			}
			if (!correct) {
				currentLanguage = defaultLanguage;
			}
		}

		return (currentLanguage != null) ? Locale.forLanguageTag(currentLanguage) : request.getLocale();
	}

	@Override
	public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
		throw new UnsupportedOperationException(
				"Cannot change fixed locale - use a different locale resolution strategy");
	}
}
