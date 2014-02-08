package org.wallride.web;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.wallride.domain.Setting;
import org.wallride.support.Settings;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class SetupCheckInterceptor extends HandlerInterceptorAdapter {

	@Inject
	private Settings settings;

	@Override
	public boolean preHandle(
			HttpServletRequest request,
			HttpServletResponse response,
			Object handler) throws Exception {
		String defaultLanguage = settings.readSettingAsString(Setting.Key.DEFAULT_LANGUAGE);
		if (StringUtils.hasText(defaultLanguage)) {
			throw new HttpForbiddenException();
		}
		return true;
	}
}
