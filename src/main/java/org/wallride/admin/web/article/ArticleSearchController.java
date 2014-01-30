package org.wallride.admin.web.article;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.admin.service.ArticleService;
import org.wallride.core.domain.Article;
import org.wallride.core.domain.Post;
import org.wallride.core.support.Paginator;
import org.wallride.core.web.DomainObjectSearchCondition;
import org.wallride.core.web.DomainObjectSearchController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@Controller @Lazy
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
			@RequestParam(required=false) String token,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response,
			HttpSession session)
			throws Exception {
		return super.requestMappingIndex(token, model, request, response, session);
	}
	
	@RequestMapping(params="page")
	public String page(
			@RequestParam int page, 
			@RequestParam(required=false) String token,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response,
			HttpSession session) {
		return super.requestMappingPage(page, token, model, request, response, session);
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

	@RequestMapping(params="part=bulk-delete-dialog")
	public String partBulkDeleteDialog() {
		return "/article/index::#bulk-delete-dialog";
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
	protected Paginator<Long> readDomainObjects(ArticleSearchForm form, int perPage) {
		List<Long> ids = articleService.searchArticles(form);
		return new Paginator<>(ids, perPage);
	}

	@Override
	protected Collection<Article> readDomainObjects(Paginator<Long> paginator) {
		return articleService.readArticles(paginator);
	}

	@Override
	protected boolean validateCondition(DomainObjectSearchCondition<ArticleSearchForm> condition, HttpServletRequest request, HttpServletResponse response) {
		String language = LocaleContextHolder.getLocale().getLanguage();
		return language.equals(condition.getForm().getLanguage());
	}
}