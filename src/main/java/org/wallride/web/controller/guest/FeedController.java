package org.wallride.web.controller.guest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.wallride.core.domain.*;
import org.wallride.core.service.ArticleSearchRequest;
import org.wallride.core.service.ArticleService;
import org.wallride.core.service.CategoryService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

@Controller
@RequestMapping("/feed")
public class FeedController {

	private static final PageRequest DEFAULT_PAGE_REQUEST = new PageRequest(0, 50);

	@Inject
	private ArticleService articleService;
	
	@Autowired
	private CategoryService categoryService;

	@RequestMapping("rss.xml")
	public String indexRss(
			BlogLanguage blogLanguage,
			Model model) {
		ArticleSearchRequest request = new ArticleSearchRequest()
				.withStatus(Post.Status.PUBLISHED)
				.withLanguage(blogLanguage.getLanguage());
		Page<Article> articles = articleService.readArticles(request, DEFAULT_PAGE_REQUEST);
		model.addAttribute("articles", new TreeSet<>(articles.getContent()));
		return "rssFeedView";
	}

	@RequestMapping("atom.xml")
	public String indexAtom(
			BlogLanguage blogLanguage,
			Model model) {
		indexRss(blogLanguage, model);
		return "atomFeedView";
	}

	@RequestMapping("category/{categoryCode}/rss.xml")
	public String categoryRss(
			@PathVariable String categoryCode,
			BlogLanguage blogLanguage,
			Model model) {
		CategoryTree categoryTree = categoryService.readCategoryTree(blogLanguage.getLanguage());
		Category category = categoryTree.getCategoryByCode(categoryCode);
		List<Long> categoryIds = new ArrayList<>();
		categoryIds.add(category.getId());

		ArticleSearchRequest request = new ArticleSearchRequest()
				.withStatus(Post.Status.PUBLISHED)
				.withLanguage(blogLanguage.getLanguage())
				.withCategoryIds(categoryIds);

		Page<Article> articles = articleService.readArticles(request, DEFAULT_PAGE_REQUEST);
		model.addAttribute("articles", new TreeSet<>(articles.getContent()));
		return "rssFeedView";
	}

	@RequestMapping("category/{categoryCode}/atom.xml")
	public String categoryAtom(
			@PathVariable String categoryCode,
			BlogLanguage blogLanguage,
			Model model) {
		categoryRss(categoryCode, blogLanguage, model);
		return "atomFeedView";
	}
}
