package org.wallride.web.controller.admin.page;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.Page;
import org.wallride.core.domain.Post;
import org.wallride.core.service.PageService;
import org.wallride.web.support.DomainObjectSearchCondition;
import org.wallride.web.support.DomainObjectSearchController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
@RequestMapping("/pages/index")
public class PageSearchController extends DomainObjectSearchController<Page, PageSearchForm> {
	
	@Inject
	private PageService pageService;

	@ModelAttribute("countAll")
	public long countAll(@PathVariable String language) {
		return pageService.countPages(language);
	}

	@ModelAttribute("countDraft")
	public long countDraft(@PathVariable String language) {
		return pageService.countPagesByStatus(Post.Status.DRAFT, language);
	}

	@ModelAttribute("countScheduled")
	public long countScheduled(@PathVariable String language) {
		return pageService.countPagesByStatus(Post.Status.SCHEDULED, language);
	}

	@ModelAttribute("countPublished")
	public long countPublished(@PathVariable String language) {
		return pageService.countPagesByStatus(Post.Status.PUBLISHED, language);
	}

	@RequestMapping(method=RequestMethod.GET)
	public String index(
			Model model,
			HttpServletRequest request,
			HttpServletResponse response,
			HttpSession session)
			throws Exception {
		return super.requestMappingIndex(model, request, response, session);
	}

	@RequestMapping(params="page")
	public String page(
			Pageable pageable,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response,
			HttpSession session) {
		return super.requestMappingPage(pageable, model, request, response, session);
	}

	@RequestMapping(params="search")
	public String search(
			@Valid PageSearchForm form,
			BindingResult result,
			Model model,
			HttpSession session,
			RedirectAttributes redirectAttributes) {
		return super.requestMappingSearch(form, result, model, session, redirectAttributes);
	}

	@RequestMapping(params="part=bulk-delete-form")
	public String partBulkDeleteDialog() {
		return "page/index::bulk-delete-form";
	}

	@Override
	protected Class<PageSearchForm> getDomainObjectSearchFormClass() {
		return PageSearchForm.class;
	}

	@Override
	protected String getModelAttributeName() {
		return "pages";
	}

	@Override
	protected String getViewName() {
		return "page/index";
	}

	@Override
	protected String getRedirectViewName() {
		return "redirect:/_admin/{language}/pages/index";
	}

	@Override
	protected org.springframework.data.domain.Page<Page> readDomainObjects(PageSearchForm form, Pageable pageable) {
		return pageService.readPages(form.buildPageSearchRequest(), pageable);
	}

	@Override
	protected boolean validateCondition(DomainObjectSearchCondition<PageSearchForm> condition, HttpServletRequest request, HttpServletResponse response) {
		String language = LocaleContextHolder.getLocale().getLanguage();
		return language.equals(condition.getForm().getLanguage());
	}
}