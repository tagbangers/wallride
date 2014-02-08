package org.wallride.core.web.guest.page;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.wallride.core.domain.Page;
import org.wallride.core.service.PageService;
import org.wallride.core.support.Paginator;
import org.wallride.core.web.DomainObjectSearchCondition;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller @Lazy
public class PageIndexController {

//	/page/[:yyyy]/[:mm]/[:dd]/[:code]
//	/categories/[:code]/[:code]/[:code]/[:code]/
//	/tag/[:code]/

	@Inject
	private PageService pageService;

	@RequestMapping("/{language}/p/")
	public String index(
			@PathVariable String language,
			@RequestParam(required=false) Integer page,
			@RequestParam(required=false) String token,
			HttpSession session,
			Model model) {
		DomainObjectSearchCondition<PageSearchForm> condition = DomainObjectSearchCondition.resolve(session, PageSearchForm.class, token);
		if (condition == null) {
			PageSearchForm form = new PageSearchForm();
			List<Long> ids = pageService.searchPages(form);
			Paginator<Long> paginator = new Paginator<>(ids, 20);
			condition = new DomainObjectSearchCondition<PageSearchForm>(session, form, paginator);
		}
		if (page != null && condition.getPaginator().hasElement()) {
			condition.getPaginator().setNumber(page);
		}

		List<Page> pages = pageService.readPages(condition.getPaginator());
		model.addAttribute("pages", pages);
		model.addAttribute("paginator", condition.getPaginator());
		return "/page/index";
	}


//	@RequestMapping("/{language}/categories/**")
//	public void category(HttpServletRequest request) {
//		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
//		String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
//		String temp = new AntPathMatcher().extractPathWithinPattern(bestMatchPattern, path);
//		System.out.println(temp);
//	}
}
