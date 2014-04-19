package org.wallride.web.support;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.UrlPathHelper;
import org.wallride.core.domain.Setting;
import org.wallride.core.support.Settings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SetupRedirectInterceptor extends HandlerInterceptorAdapter {

    private static final String SETUP_PATH = "/_admin/setup";

	private Settings settings;

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	@Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String defaultLanguage = settings.readSettingAsString(Setting.Key.DEFAULT_LANGUAGE);
		if (StringUtils.hasText(defaultLanguage)) {
            return true;
        }

        final String requestPath = getRequestPath(request);
        if (!SETUP_PATH.equalsIgnoreCase(requestPath)) {
            response.sendRedirect(request.getContextPath() + SETUP_PATH);
            return false;
        }

        return true;
    }

    private String getRequestPath(HttpServletRequest request) {
		UrlPathHelper urlPathHelper = new UrlPathHelper();
		return urlPathHelper.getPathWithinApplication(request);
//        return (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    }
}
