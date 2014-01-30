package org.wallride.admin.web;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.admin.service.ArticleService;
import org.wallride.admin.service.PageService;
import org.wallride.admin.web.article.ArticleSearchForm;
import org.wallride.core.domain.Article;
import org.wallride.core.domain.CategoryTree;
import org.wallride.core.domain.Post;
import org.wallride.core.domain.Setting;
import org.wallride.core.service.CategoryTreeService;
import org.wallride.core.support.Paginator;
import org.wallride.core.support.Settings;

import javax.inject.Inject;
import java.util.List;

@Controller @Lazy
public class DashboardController {
	
	@Inject
	private Settings settings;

	@Inject
	private ArticleService articleService;

	@Inject
	private PageService pageService;

	@Inject
	private CategoryTreeService categoryTreeService;
	
	@RequestMapping({"/","/dashboard"})
	public String dashboard(RedirectAttributes redirectAttributes) {
		String defaultLanguage = settings.readSettingAsString(Setting.Key.DEFAULT_LANGUAGE);
		redirectAttributes.addAttribute("language", defaultLanguage);
		return "redirect:/_admin/{language}/";
	}
	
	@RequestMapping("/{language}/")
	public String dashboard(@PathVariable String language, Model model) {
		long articleCount = articleService.countArticlesByStatus(Post.Status.PUBLISHED, language);
		long pageCount = pageService.countPagesByStatus(Post.Status.PUBLISHED, language);

		CategoryTree categoryTreeHasArticle = categoryTreeService.readCategoryTree(language, true);
		long categoryCount = categoryTreeHasArticle.getCategories().size();

		model.addAttribute("articleCount", articleCount);
		model.addAttribute("pageCount", pageCount);
		model.addAttribute("categoryCount", categoryCount);
		model.addAttribute("recentPublishedArticles", recentPublishedArticles(language));
		model.addAttribute("recentDraftArticles", recentDraftArtciles(language));

		return "/dashboard";
	}

	private List<Article> recentPublishedArticles(String language) {
		ArticleSearchForm form = new ArticleSearchForm();
		form.setLanguage(language);
		form.setStatus(Post.Status.PUBLISHED);
		List<Long> ids = articleService.searchArticles(form);
		Paginator<Long> paginator = new Paginator<>(ids, 10);
		return articleService.readArticles(paginator);
	}

	private List<Article> recentDraftArtciles(String language) {
		ArticleSearchForm form = new ArticleSearchForm();
		form.setLanguage(language);
		form.setStatus(Post.Status.DRAFT);
		List<Long> ids = articleService.searchArticles(form);
		Paginator<Long> paginator = new Paginator<>(ids, 10);
		return articleService.readArticles(paginator);
	}
}
