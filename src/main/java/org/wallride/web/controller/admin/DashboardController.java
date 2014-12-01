package org.wallride.web.controller.admin;

import org.joda.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.Article;
import org.wallride.core.domain.Blog;
import org.wallride.core.domain.CategoryTree;
import org.wallride.core.domain.Post;
import org.wallride.core.service.*;
import org.wallride.web.controller.admin.article.ArticleSearchForm;

import javax.inject.Inject;
import java.util.List;

@Controller
public class DashboardController {

	private static final int POPULAR_POSTS_DAYS = 7;
	private static final int POPULAR_POSTS_COUNT = 10;

	@Inject
	private BlogService blogService;
	@Inject
	private PostService postService;
	@Inject
	private ArticleService articleService;
	@Inject
	private PageService pageService;
	@Inject
	private CategoryService categoryService;
	
	@RequestMapping({"/","/dashboard"})
	public String dashboard(RedirectAttributes redirectAttributes) {
		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);
		String defaultLanguage = blog.getDefaultLanguage();
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
		model.addAttribute("popularPosts", popularPosts(language));
		model.addAttribute("recentPublishedArticles", recentPublishedArticles(language));
		model.addAttribute("recentDraftArticles", recentDraftArticles(language));

		return "dashboard";
	}

	private List<Post> popularPosts(String language) {
		return postService.readPopularPosts(LocalDate.now().minusDays(POPULAR_POSTS_DAYS), language, POPULAR_POSTS_COUNT);
	}

	private List<Article> recentPublishedArticles(String language) {
		ArticleSearchForm form = new ArticleSearchForm();
		form.setLanguage(language);
		form.setStatus(Post.Status.PUBLISHED);
		Page<Article> page = articleService.readArticles(form.buildArticleSearchRequest());
		return page.getContent();
	}

	private List<Article> recentDraftArticles(String language) {
		ArticleSearchForm form = new ArticleSearchForm();
		form.setLanguage(language);
		form.setStatus(Post.Status.DRAFT);
		Page<Article> page = articleService.readArticles(form.buildArticleSearchRequest());
		return page.getContent();
	}
}
