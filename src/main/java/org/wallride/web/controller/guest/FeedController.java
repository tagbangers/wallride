package org.wallride.web.controller.guest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.wallride.core.domain.Article;
import org.wallride.core.domain.Category;
import org.wallride.core.domain.CategoryTree;
import org.wallride.core.domain.Post;
import org.wallride.core.service.ArticleSearchRequest;
import org.wallride.core.service.ArticleService;
import org.wallride.core.service.CategoryService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

@Controller
@RequestMapping("/{language}/feed")
public class FeedController {

	private static final PageRequest DEFAULT_PAGE_REQUEST = new PageRequest(0, 50);

	@Inject
	private ArticleService articleService;
	
	@Autowired
	private CategoryService categoryService;

	@RequestMapping("rss.xml")
	public String indexRss(
			@PathVariable String language,
			Model model) {
		ArticleSearchRequest request = new ArticleSearchRequest.Builder()
				.status(Post.Status.PUBLISHED)
				.language(language)
				.build();
		Page<Article> articles = articleService.readArticles(request, DEFAULT_PAGE_REQUEST);
		model.addAttribute("articles", new TreeSet<>(articles.getContent()));
		return "rssFeedView";
	}

	@RequestMapping("atom.xml")
	public String indexAtom(
			@PathVariable String language,
			Model model) {
		indexRss(language, model);
		return "atomFeedView";
	}

	@RequestMapping("category/{categoryCode}/rss.xml")
	public String categoryRss(
			@PathVariable String language,
			@PathVariable String categoryCode,
			Model model) {
		CategoryTree categoryTree = categoryService.readCategoryTree(language);
		Category category = categoryTree.getCategoryByCode(categoryCode);
		List<Long> categoryIds = new ArrayList<>();
		categoryIds.add(category.getId());

		ArticleSearchRequest request = new ArticleSearchRequest.Builder()
				.status(Post.Status.PUBLISHED)
				.language(language)
				.categoryIds(categoryIds)
				.build();

		Page<Article> articles = articleService.readArticles(request, DEFAULT_PAGE_REQUEST);
		model.addAttribute("articles", new TreeSet<>(articles.getContent()));
		return "rssFeedView";
	}

	@RequestMapping("category/{categoryCode}/atom.xml")
	public String categoryAtom(
			@PathVariable String language,
			@PathVariable String categoryCode,
			Model model) {
		categoryRss(language, categoryCode, model);
		return "atomFeedView";
	}
}
