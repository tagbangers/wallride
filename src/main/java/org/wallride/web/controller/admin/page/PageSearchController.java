package org.wallride.web.controller.admin.page;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriComponentsBuilder;
import org.wallride.Application;
import org.wallride.core.domain.Page;
import org.wallride.core.domain.Post;
import org.wallride.core.service.PageService;
import org.wallride.core.support.Pagination;
import org.wallride.web.controller.admin.article.ArticleSearchForm;
import org.wallride.web.support.DomainObjectSearchCondition;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/{language}/pages/index")
public class PageSearchController {
	
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

	@ModelAttribute("form")
	public PageSearchForm setupPageSearchForm() {
		return new PageSearchForm();
	}

	@RequestMapping(method = RequestMethod.GET)
	public String search(
			@PathVariable String language,
			@Validated PageSearchForm form,
			BindingResult result,
			@PageableDefault(50) Pageable pageable,
			Model model,
			HttpSession session) {
		org.springframework.data.domain.Page<Page> pages = pageService.readPages(form.toPageSearchRequest(), pageable);

		new DomainObjectSearchCondition<>(session, form, pageable);

		model.addAttribute("form", form);
		model.addAttribute("pages", pages);
		model.addAttribute("pageable", pageable);

		UriComponentsBuilder builder = UriComponentsBuilder.fromPath(Application.ADMIN_SERVLET_PATH);
		builder.path("/{language}/pages/index");
		builder.queryParams(form.toQueryParams());
		String url = builder.buildAndExpand(language).toString();
		model.addAttribute("pagination", new Pagination<>(pages, url));
		return "page/index";
	}


	@RequestMapping(method = RequestMethod.GET, params = "part=bulk-delete-form")
	public String partBulkDeleteForm(@PathVariable String language) {
		return "page/index::bulk-delete-form";
	}

	@RequestMapping(method = RequestMethod.GET, params = "part=bulk-publish-form")
	public String partBulkPublishForm(@PathVariable String language) {
		return "page/index::bulk-publish-form";
	}

	@RequestMapping(method = RequestMethod.GET, params = "part=bulk-unpublish-form")
	public String partBulkUnpublishForm(@PathVariable String language) {
		return "page/index::bulk-unpublish-form";
	}
}