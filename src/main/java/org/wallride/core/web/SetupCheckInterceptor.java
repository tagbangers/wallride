package org.wallride.core.web;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.wallride.core.domain.Setting;
import org.wallride.core.service.SettingService;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class SetupCheckInterceptor extends HandlerInterceptorAdapter {

	@Inject
	private SettingService settingService;

	@Override
	public boolean preHandle(
			HttpServletRequest request,
			HttpServletResponse response,
			Object handler) throws Exception {
		String defaultLanguage = settingService.readSettingAsString(Setting.Key.DEFAULT_LANGUAGE);
		if (StringUtils.hasText(defaultLanguage)) {
			throw new HttpForbiddenException();
		}
		return true;
	}
}
