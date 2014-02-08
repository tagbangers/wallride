package org.wallride.web.admin;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.domain.Article;
import org.wallride.domain.CategoryTree;
import org.wallride.domain.Post;
import org.wallride.domain.Setting;
import org.wallride.service.ArticleService;
import org.wallride.service.CategoryService;
import org.wallride.service.PageService;
import org.wallride.support.Paginator;
import org.wallride.support.Settings;
import org.wallride.web.admin.article.ArticleSearchForm;

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
	private CategoryService categoryService;
	
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

		CategoryTree categoryTreeHasArticle = categoryService.readCategoryTree(language, true);
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
		List<Long> ids = articleService.searchArticles(form.buildArticleSearchRequest());
		Paginator<Long> paginator = new Paginator<>(ids, 10);
		return articleService.readArticles(paginator);
	}

	private List<Article> recentDraftArtciles(String language) {
		ArticleSearchForm form = new ArticleSearchForm();
		form.setLanguage(language);
		form.setStatus(Post.Status.DRAFT);
		List<Long> ids = articleService.searchArticles(form.buildArticleSearchRequest());
		Paginator<Long> paginator = new Paginator<>(ids, 10);
		return articleService.readArticles(paginator);
	}
}
