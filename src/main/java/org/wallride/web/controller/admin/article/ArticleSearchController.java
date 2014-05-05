package org.wallride.web.controller.admin.article;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.Article;
import org.wallride.core.domain.Post;
import org.wallride.core.service.ArticleService;
import org.wallride.web.support.DomainObjectSearchCondition;
import org.wallride.web.support.DomainObjectSearchController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
@RequestMapping("/{language}/articles/index")
public class ArticleSearchController extends DomainObjectSearchController<Article, ArticleSearchForm> {

	@Inject
	private ArticleService articleService;

	@ModelAttribute("countAll")
	public long countAll(@PathVariable String language) {
		return articleService.countArticles(language);
	}

	@ModelAttribute("countDraft")
	public long countDraft(@PathVariable String language) {
		return articleService.countArticlesByStatus(Post.Status.DRAFT, language);
	}

	@ModelAttribute("countScheduled")
	public long countScheduled(@PathVariable String language) {
		return articleService.countArticlesByStatus(Post.Status.SCHEDULED, language);
	}

	@ModelAttribute("countPublished")
	public long countPublished(@PathVariable String language) {
		return articleService.countArticlesByStatus(Post.Status.PUBLISHED, language);
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
			@PageableDefault(50) Pageable pageable,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response,
			HttpSession session) {
		return super.requestMappingPage(pageable, model, request, response, session);
	}

	@RequestMapping(params="search")
	public String search(
			@Valid ArticleSearchForm form,
			BindingResult result,
			Model model,
			HttpSession session,
			RedirectAttributes redirectAttributes) {
		return super.requestMappingSearch(form, result, model, session, redirectAttributes);
	}

	@RequestMapping(params="part=bulk-delete-form")
	public String partBulkDeleteDialog() {
		return "/article/index::bulk-delete-form";
	}

	@Override
	protected Class<ArticleSearchForm> getDomainObjectSearchFormClass() {
		return ArticleSearchForm.class;
	}

	@Override
	protected String getModelAttributeName() {
		return "articles";
	}

	@Override
	protected String getViewName() {
		return "/article/index";
	}

	@Override
	protected String getRedirectViewName() {
		return "redirect:/_admin/{language}/articles/index";
	}

	@Override
	protected Page<Article> readDomainObjects(ArticleSearchForm form, Pageable pageable) {
		return articleService.readArticles(form.buildArticleSearchRequest(), pageable);
	}

	@Override
	protected boolean validateCondition(DomainObjectSearchCondition<ArticleSearchForm> condition, HttpServletRequest request, HttpServletResponse response) {
		String language = LocaleContextHolder.getLocale().getLanguage();
		return language.equals(condition.getForm().getLanguage());
	}
}