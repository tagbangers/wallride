package org.wallride.web.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UrlPathHelper;
import org.wallride.core.domain.*;
import org.wallride.core.service.BlogService;
import org.wallride.core.service.CategoryService;
import org.wallride.core.service.PageService;
import org.wallride.core.support.AuthorizedUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultModelAttributeInterceptor extends HandlerInterceptorAdapter {

	private BlogService blogService;
	private CategoryService categoryService;
	private PageService pageService;

	private static Logger logger = LoggerFactory.getLogger(DefaultModelAttributeInterceptor.class);

	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}

	public void setCategoryService(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	public void setPageService(PageService pageService) {
		this.pageService = pageService;
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

		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);
		List<String> languages = new ArrayList<>();
		for (BlogLanguage blogLanguage : blog.getLanguages()) {
			languages.add(blogLanguage.getLanguage());
		}

		String currentLanguage = LocaleContextHolder.getLocale().getLanguage();

		mv.addObject("LANGUAGES", languages.toArray(new String[languages.size()]));
		mv.addObject("LANGUAGE_LINKS", buildLanguageLinks(currentLanguage, languages, request));

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		AuthorizedUser authorizedUser = null;
		if (authentication != null && authentication.getPrincipal() instanceof AuthorizedUser) {
			authorizedUser = (AuthorizedUser) authentication.getPrincipal();
		}
		mv.addObject("USER", authorizedUser);

		mv.addObject("WEBSITE_TITLE", blog.getTitle(currentLanguage));
		mv.addObject("WEBSITE_LINK", buildGuestLink());
		mv.addObject("WEBSITE_PATH", buildGuestPath(currentLanguage, languages));

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

	private String buildGuestLink() {
		UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath();
		return builder.buildAndExpand().toUriString();
	}

	private String buildGuestPath(String currentLanguage, List<String> languages) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("");
		if (languages.size() > 1) {
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

	private Map<String, String> buildLanguageLinks(String currentLanguage, List<String> languages, HttpServletRequest request) {
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
