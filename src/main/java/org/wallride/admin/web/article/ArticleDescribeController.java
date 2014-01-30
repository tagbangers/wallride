package org.wallride.admin.web.article;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.wallride.admin.service.ArticleService;
import org.wallride.core.domain.Article;
import org.wallride.core.web.DomainObjectDescribeController;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

@Controller @Lazy
@RequestMapping(value="/{language}/articles/describe", method=RequestMethod.GET)
public class ArticleDescribeController extends DomainObjectDescribeController<Article, ArticleSearchForm> {
	
	@Inject
	private ArticleService articleService;
	
	@RequestMapping
	public String describe( 
			@RequestParam long id,
			@RequestParam(required=false) String token,
			Model model,
			HttpSession session) {
		return super.requestMappingDescribe(id, token, model, session);
	}

	@RequestMapping(params="part=delete-dialog")
	public String partDeleteDialog(
			@PathVariable String language,
			@RequestParam long id, Model model) {
		Article article = articleService.readArticleById(id, language);
		model.addAttribute("article", article);
		return "/article/describe::#delete-dialog";
	}

	@Override
	protected Class<ArticleSearchForm> getDomainObjectSearchFormClass() {
		return ArticleSearchForm.class;
	}

	@Override
	protected String getModelAttributeName() {
		return "article";
	}

	@Override
	protected String getViewName() {
		return "/article/describe";
	}

	@Override
	protected Article readDomainObject(long id) {
		return articleService.readArticleById(id, LocaleContextHolder.getLocale().getLanguage());
	}
}