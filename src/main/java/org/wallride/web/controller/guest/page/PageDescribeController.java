package org.wallride.web.controller.guest.page;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.util.UrlPathHelper;
import org.wallride.core.domain.Page;
import org.wallride.core.service.PageService;
import org.wallride.core.support.Settings;
import org.wallride.web.support.HttpNotFoundException;
import org.wallride.web.support.LanguageUrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class PageDescribeController extends AbstractController {

	private static final String PATH_PATTERN = "/{language}/{code}";

	private PageService pageService;
	private UrlPathHelper urlPathHelper;

	public PageDescribeController(PageService pageService, Settings settings) {
		this.pageService = pageService;
		this.urlPathHelper = new LanguageUrlPathHelper(settings);
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String path = urlPathHelper.getLookupPathForRequest(request);

		PathMatcher pathMatcher = new AntPathMatcher();
		if (!pathMatcher.match(PATH_PATTERN, path)) {
			throw new HttpNotFoundException();
		}

		Map<String, String> variables = pathMatcher.extractUriTemplateVariables(PATH_PATTERN, path);
		request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, variables);

		Page page = pageService.readPageByCode(variables.get("code"), variables.get("language"));
		if (page == null) {
			throw new HttpNotFoundException();
		}

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("/page/describe");
		modelAndView.addObject("page", page);
		return modelAndView;
	}
}
