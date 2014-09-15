package org.wallride.web.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.LocaleResolver;
import org.wallride.core.domain.Blog;
import org.wallride.core.domain.BlogLanguage;
import org.wallride.core.domain.Setting;
import org.wallride.core.service.BlogService;
import org.wallride.core.support.Settings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Map;

public class PathVariableLocaleResolver implements LocaleResolver {

	private BlogService blogService;

	private static Logger logger = LoggerFactory.getLogger(PathVariableLocaleResolver.class);

	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}

	@Override
	public Locale resolveLocale(HttpServletRequest request) {
		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);

		Map<String, Object> pathVariables = (Map<String, Object>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
		String currentLanguage = (String) pathVariables.get("language");
		if (currentLanguage == null) {
			currentLanguage = blog.getDefaultLanguage();
		}
		else {
			boolean correct = false;
			for (BlogLanguage blogLanguage : blog.getLanguages()) {
				if (blogLanguage.getLanguage().equals(currentLanguage)) {
					correct = true;
					break;
				}
			}
			if (!correct) {
				currentLanguage = blog.getDefaultLanguage();
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
