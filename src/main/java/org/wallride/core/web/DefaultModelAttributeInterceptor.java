package org.wallride.core.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UrlPathHelper;
import org.wallride.admin.support.AuthorizedUser;
import org.wallride.core.domain.CategoryTree;
import org.wallride.core.domain.PageTree;
import org.wallride.core.domain.Post;
import org.wallride.core.domain.Setting;
import org.wallride.core.service.CategoryTreeService;
import org.wallride.core.service.PageTreeService;
import org.wallride.core.service.SettingService;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DefaultModelAttributeInterceptor extends HandlerInterceptorAdapter {
	
	@Inject
	private SettingService settingService;

	@Inject
	private CategoryTreeService categoryTreeService;

	@Inject
	private PageTreeService pageTreeService;

	@Inject
	private Environment environment;

	private static Logger logger = LoggerFactory.getLogger(DefaultModelAttributeInterceptor.class);

	@Override
	public boolean preHandle(
			HttpServletRequest request,
			HttpServletResponse response,
			Object handler) throws Exception {
		return true;
	}

	@Override
	public void postHandle(
			HttpServletRequest request,
			HttpServletResponse response,
			Object handler,
			ModelAndView mv)
			throws Exception {
		if (mv == null) return;
		if (mv.getView() instanceof RedirectView) return;
		if (mv.getViewName().startsWith("redirect:")) return;

		String currentLanguage = LocaleContextHolder.getLocale().getLanguage();

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		AuthorizedUser authorizedUser = null;
		if (authentication != null && authentication.getPrincipal() instanceof AuthorizedUser) {
			authorizedUser = (AuthorizedUser) authentication.getPrincipal();
		}
		mv.addObject("USER", authorizedUser);

		mv.addObject("WEBSITE_TITLE", settingService.readSettingAsString(Setting.Key.WEBSITE_TITLE, currentLanguage));
		mv.addObject("WEBSITE_LINK", buildBlogLink());
		mv.addObject("WEBSITE_PATH", buildBlogPath());

		mv.addObject("ADMIN_LINK", buildAdminLink());
		mv.addObject("ADMIN_PATH", buildAdminPath());

		mv.addObject("LANGUAGE_LINKS", buildLanguageLinks(request));

		CategoryTree categoryTreeHasArticle = categoryTreeService.readCategoryTree(currentLanguage, true);
		CategoryTree categoryTreeAll = categoryTreeService.readCategoryTree(currentLanguage);
		mv.addObject("CATEGORY_TREE", categoryTreeHasArticle);
		mv.addObject("CATEGORY_TREE_ALL", categoryTreeAll);

		PageTree pageTreePublished = pageTreeService.readPageTree(currentLanguage, Post.Status.PUBLISHED);
		PageTree pageTreeAll = pageTreeService.readPageTree(currentLanguage);
		mv.addObject("PAGE_TREE", pageTreePublished);
		mv.addObject("PAGE_TREE_ALL", pageTreeAll);
	}

	private String buildBlogLink() {
		UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath();
		return builder.buildAndExpand().toUriString();
	}

	private String buildBlogPath() {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("");
		String[] languages = settingService.readSettingAsStringArray(Setting.Key.LANGUAGES, ",");
		if (languages != null && languages.length > 1) {
			builder.path("/{language}");
		}
		return builder.buildAndExpand(LocaleContextHolder.getLocale().getLanguage()).toUriString();
	}

	private String buildAdminLink() {
		UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath();
		builder.path("/_admin");
		return builder.buildAndExpand().toUriString();
	}

	private String buildAdminPath() {
//		String contextPath = request.getContextPath();
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/_admin");
		builder.path("/{language}");
		return builder.buildAndExpand(LocaleContextHolder.getLocale().getLanguage()).toUriString();
	}

	private Map<String, String> buildLanguageLinks(HttpServletRequest request) {
		UrlPathHelper pathHelper = new UrlPathHelper();
		Map<String, String> languageLinks = new LinkedHashMap<>();
		String path = pathHelper.getPathWithinServletMapping(request);
		if (path.startsWith("/" + LocaleContextHolder.getLocale().getLanguage() + "/")) {
			path = path.substring(LocaleContextHolder.getLocale().getLanguage().length() + 1);
		}
		UriComponentsBuilder uriComponentsBuilder = ServletUriComponentsBuilder
				.fromCurrentServletMapping()
				.path("/{language}")
				.path(path)
				.query(pathHelper.getOriginatingQueryString(request));
		String[] languages = settingService.readSettingAsStringArray(Setting.Key.LANGUAGES, ",");
		if (languages != null) {
			for (String language : languages) {
				languageLinks.put(language, uriComponentsBuilder.buildAndExpand(language).toUriString());
			}
		}
		return languageLinks;
	}
}
