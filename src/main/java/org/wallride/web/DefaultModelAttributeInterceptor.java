package org.wallride.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UrlPathHelper;
import org.wallride.domain.CategoryTree;
import org.wallride.domain.PageTree;
import org.wallride.domain.Post;
import org.wallride.domain.Setting;
import org.wallride.service.CategoryService;
import org.wallride.service.PageService;
import org.wallride.support.AuthorizedUser;
import org.wallride.support.Settings;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DefaultModelAttributeInterceptor extends HandlerInterceptorAdapter {

	@Inject
	private Settings settings;

	@Inject
	private CategoryService categoryService;

	@Inject
	private PageService pageService;

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

		String[] languages = settings.readSettingAsStringArray(Setting.Key.LANGUAGES, ",");
		String currentLanguage = LocaleContextHolder.getLocale().getLanguage();

		mv.addObject("LANGUAGES", languages);
		mv.addObject("LANGUAGE_LINKS", buildLanguageLinks(currentLanguage, languages, request));

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		AuthorizedUser authorizedUser = null;
		if (authentication != null && authentication.getPrincipal() instanceof AuthorizedUser) {
			authorizedUser = (AuthorizedUser) authentication.getPrincipal();
		}
		mv.addObject("USER", authorizedUser);

		mv.addObject("WEBSITE_TITLE", settings.readSettingAsString(Setting.Key.WEBSITE_TITLE, currentLanguage));
		mv.addObject("WEBSITE_LINK", buildBlogLink());
		mv.addObject("WEBSITE_PATH", buildBlogPath(currentLanguage, languages));

		mv.addObject("ADMIN_LINK", buildAdminLink());
		mv.addObject("ADMIN_PATH", buildAdminPath(currentLanguage));

		CategoryTree categoryTreeHasArticle = categoryService.readCategoryTree(currentLanguage, true);
		CategoryTree categoryTreeAll = categoryService.readCategoryTree(currentLanguage);
		mv.addObject("CATEGORY_TREE", categoryTreeHasArticle);
		mv.addObject("CATEGORY_TREE_ALL", categoryTreeAll);

		PageTree pageTreePublished = pageService.readPageTree(currentLanguage, Post.Status.PUBLISHED);
		PageTree pageTreeAll = pageService.readPageTree(currentLanguage);
		mv.addObject("PAGE_TREE", pageTreePublished);
		mv.addObject("PAGE_TREE_ALL", pageTreeAll);

	}

	private String buildBlogLink() {
		UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath();
		return builder.buildAndExpand().toUriString();
	}

	private String buildBlogPath(String currentLanguage, String[] languages) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("");
		if (languages != null && languages.length > 1) {
			builder.path("/{language}");
		}
		return builder.buildAndExpand(currentLanguage).toUriString();
	}

	private String buildAdminLink() {
		UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath();
		builder.path("/_admin");
		return builder.buildAndExpand().toUriString();
	}

	private String buildAdminPath(String currentLanguage) {
//		String contextPath = request.getContextPath();
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/_admin");
		builder.path("/{language}");
		return builder.buildAndExpand(currentLanguage).toUriString();
	}

	private Map<String, String> buildLanguageLinks(String currentLanguage, String[] languages, HttpServletRequest request) {
		UrlPathHelper pathHelper = new UrlPathHelper();
		Map<String, String> languageLinks = new LinkedHashMap<>();
		String path = pathHelper.getPathWithinServletMapping(request);
		if (path.startsWith("/" + currentLanguage + "/")) {
			path = path.substring(currentLanguage.length() + 1);
		}
		UriComponentsBuilder uriComponentsBuilder = ServletUriComponentsBuilder
				.fromCurrentServletMapping()
				.path("/{language}")
				.path(path)
				.query(pathHelper.getOriginatingQueryString(request));
		if (languages != null) {
			for (String language : languages) {
				languageLinks.put(language, uriComponentsBuilder.buildAndExpand(language).toUriString());
			}
		}
		return languageLinks;
	}
}
