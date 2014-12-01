package org.wallride.web.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.support.RequestDataValueProcessor;
import org.springframework.web.util.UrlPathHelper;
import org.wallride.core.domain.BlogLanguage;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class BlogLanguageDataValueProcessor implements RequestDataValueProcessor {

	private static Logger logger = LoggerFactory.getLogger(BlogLanguageDataValueProcessor.class);

	@Override
	public String processAction(HttpServletRequest request, String action, String httpMethod) {
		return processUrl(request, action);
	}

	@Override
	public String processFormFieldValue(HttpServletRequest request, String name, String value, String type) {
		return value;
	}

	@Override
	public Map<String, String> getExtraHiddenFields(HttpServletRequest request) {
		return null;
	}

	@Override
	public String processUrl(HttpServletRequest request, String url) {
		UrlPathHelper urlPathHelper = new UrlPathHelper();
		String contextPath = urlPathHelper.getContextPath(request);

		if (!url.startsWith(contextPath + "/")) {
			return url;
		}

		BlogLanguage blogLanguage = (BlogLanguage) request.getAttribute(BlogLanguageMethodArgumentResolver.BLOG_LANGUAGE_ATTRIBUTE);
		if (blogLanguage.getBlog().getLanguages().size() <= 1) {
			return url;
		}

		String path = url.substring(contextPath.length());
		String processedUrl = contextPath + "/" + blogLanguage.getLanguage() + path;
		logger.debug(url + " => " + processedUrl);
		return processedUrl;
	}
}
