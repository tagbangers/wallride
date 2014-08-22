package org.wallride.web.controller.guest.article;

import org.joda.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerMapping;
import org.wallride.core.domain.Article;
import org.wallride.core.domain.Category;
import org.wallride.core.domain.CategoryTree;
import org.wallride.core.domain.Tag;
import org.wallride.core.service.ArticleService;
import org.wallride.core.service.CategoryService;
import org.wallride.core.service.TagService;
import org.wallride.core.support.Pagination;
import org.wallride.web.support.HttpNotFoundException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class ArticleIndexController {

//	/article/[:yyyy]/[:mm]/[:dd]/[:code]
//	/categories/[:code]/[:code]/[:code]/[:code]/
//	/tag/[:code]/

	@Inject
	private ArticleService articleService;
	@Inject
	private CategoryService categoryService;
	@Inject
	private TagService tagService;

	@RequestMapping("/{language}/")
	public String index(
			@PathVariable String language,
			@PageableDefault(10) Pageable pageable,
			HttpSession session,
			Model model) {
		ArticleSearchForm form = new ArticleSearchForm() {};
		form.setLanguage(language);

		Page<Article> articles = articleService.readArticles(form.buildArticleSearchRequest(), pageable);
		model.addAttribute("articles", articles);
		model.addAttribute("pageable", pageable);
		model.addAttribute("pagination", new Pagination<>(articles));
		return "/article/index";
	}

	@RequestMapping("/{language}/{year:[0-9]{4}}")
	public String year(
			@PathVariable String language,
			@PathVariable int year,
			@PageableDefault(10) Pageable pageable,
			Model model) {
		ArticleSearchForm form = new ArticleSearchForm() {};
		form.setLanguage(language);
		form.setDateFrom(new LocalDateTime(year, 1, 1, 0, 0, 0));
		form.setDateTo(new LocalDateTime(year, 12, 31, 0, 0, 0));

		Page<Article> articles = articleService.readArticles(form.buildArticleSearchRequest(), pageable);
		model.addAttribute("articles", articles);
		model.addAttribute("pageable", pageable);
		model.addAttribute("pagination", new Pagination<>(articles));
		return "/article/index";
	}

	@RequestMapping("/{language}/{year:[0-9]{4}}/{month:[0-9]{2}}")
	public String month(
			@PathVariable String language,
			@PathVariable int year,
			@PathVariable int month,
			@PageableDefault(10) Pageable pageable,
			Model model) {
		ArticleSearchForm form = new ArticleSearchForm() {};
		form.setLanguage(language);
		LocalDateTime date = new LocalDateTime(year, month, 1, 0, 0, 0);
		form.setDateFrom(new LocalDateTime(year, month, 1, 0, 0, 0));
		form.setDateTo(new LocalDateTime(year, month, date.dayOfMonth().getMaximumValue(), 23, 59, 59));

		Page<Article> articles = articleService.readArticles(form.buildArticleSearchRequest(), pageable);
		model.addAttribute("articles", articles);
		model.addAttribute("pageable", pageable);
		model.addAttribute("pagination", new Pagination<>(articles));
		return "/article/index";
	}

	@RequestMapping("/{language}/{year:[0-9]{4}}/{month:[0-9]{2}}/{day:[0-9]{2}}")
	public String day(
			@PathVariable String language,
			@PathVariable int year,
			@PathVariable int month,
			@PathVariable int day,
			@PageableDefault(10) Pageable pageable,
			Model model) {
		ArticleSearchForm form = new ArticleSearchForm() {};
		form.setLanguage(language);
		form.setDateFrom(new LocalDateTime(year, month, day, 0, 0, 0));
		form.setDateTo(new LocalDateTime(year, month, day, 23, 59, 59));

		Page<Article> articles = articleService.readArticles(form.buildArticleSearchRequest(), pageable);
		model.addAttribute("articles", articles);
		model.addAttribute("pageable", pageable);
		model.addAttribute("pagination", new Pagination<>(articles));
		return "/article/index";
	}

	@RequestMapping("/{language}/category/**")
	public String category(
			@PathVariable String language,
			@PageableDefault(10) Pageable pageable,
			HttpServletRequest request,
			Model model) {
		String path = extractPathFromPattern(request);
		String[] codes = path.split("/");
		String lastCode = codes[codes.length - 1];

		CategoryTree categoryTree = categoryService.readCategoryTree(language);
		Category category = categoryTree.getCategoryByCode(lastCode);

		ArticleSearchForm form = new ArticleSearchForm() {};
		form.setLanguage(language);
		form.getCategoryIds().add(category.getId());

		Page<Article> articles = articleService.readArticles(form.buildArticleSearchRequest(), pageable);
		model.addAttribute("category", category);
		model.addAttribute("articles", articles);
		model.addAttribute("pageable", pageable);
		model.addAttribute("pagination", new Pagination<>(articles));
		return "/article/index";
	}

	private String extractPathFromPattern(final HttpServletRequest request){
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		String bestMatchPattern = (String ) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

		AntPathMatcher apm = new AntPathMatcher();
		String finalPath = apm.extractPathWithinPattern(bestMatchPattern, path);

		return finalPath;
	}

	@RequestMapping("/{language}/tag/{name}")
	public String tag(
			@PathVariable String language,
			@PathVariable String name,
			@PageableDefault(10) Pageable pageable,
			HttpServletRequest request,
			Model model) {
		Tag tag = tagService.readTagByName(name, language);
		if (tag == null) {
			throw new HttpNotFoundException();
		}

		ArticleSearchForm form = new ArticleSearchForm() {};
		form.setLanguage(language);
		form.getTagIds().add(tag.getId());

		Page<Article> articles = articleService.readArticles(form.buildArticleSearchRequest(), pageable);
		model.addAttribute("tag", tag);
		model.addAttribute("articles", articles);
		model.addAttribute("pageable", pageable);
		model.addAttribute("pagination", new Pagination<>(articles));
		return "/article/index";
	}
}
