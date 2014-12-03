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
import org.wallride.core.domain.*;
import org.wallride.core.service.*;
import org.wallride.core.support.Pagination;
import org.wallride.web.support.HttpNotFoundException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

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
	@Inject
	private UserService userService;

//	@RequestMapping("/")
//	public String index(
//			@PathVariable String language,
//			@PageableDefault(10) Pageable pageable,
//			HttpSession session,
//			Model model) {
//		ArticleSearchForm form = new ArticleSearchForm() {};
//		form.setLanguage(language);
//
//		Page<Article> articles = articleService.readArticles(form.toArticleSearchRequest(), pageable);
//		model.addAttribute("articles", articles);
//		model.addAttribute("pageable", pageable);
//		model.addAttribute("pagination", new Pagination<>(articles));
//		return "article/index";
//	}

	@RequestMapping("/{year:[0-9]{4}}")
	public String year(
			@PathVariable int year,
			@PageableDefault(10) Pageable pageable,
			BlogLanguage blogLanguage,
			Model model) {
		ArticleSearchForm form = new ArticleSearchForm() {};
		form.setLanguage(blogLanguage.getLanguage());
		form.setDateFrom(new LocalDateTime(year, 1, 1, 0, 0, 0));
		form.setDateTo(new LocalDateTime(year, 12, 31, 0, 0, 0));

		Page<Article> articles = articleService.readArticles(form.toArticleSearchRequest(), pageable);
		model.addAttribute("articles", articles);
		model.addAttribute("pageable", pageable);
		model.addAttribute("pagination", new Pagination<>(articles));
		return "article/index";
	}

	@RequestMapping("/{year:[0-9]{4}}/{month:[0-9]{2}}")
	public String month(
			@PathVariable int year,
			@PathVariable int month,
			@PageableDefault(10) Pageable pageable,
			BlogLanguage blogLanguage,
			Model model) {
		ArticleSearchForm form = new ArticleSearchForm() {};
		form.setLanguage(blogLanguage.getLanguage());
		LocalDateTime date = new LocalDateTime(year, month, 1, 0, 0, 0);
		form.setDateFrom(new LocalDateTime(year, month, 1, 0, 0, 0));
		form.setDateTo(new LocalDateTime(year, month, date.dayOfMonth().getMaximumValue(), 23, 59, 59));

		Page<Article> articles = articleService.readArticles(form.toArticleSearchRequest(), pageable);
		model.addAttribute("articles", articles);
		model.addAttribute("pageable", pageable);
		model.addAttribute("pagination", new Pagination<>(articles));
		return "article/index";
	}

	@RequestMapping("/{year:[0-9]{4}}/{month:[0-9]{2}}/{day:[0-9]{2}}")
	public String day(
			@PathVariable int year,
			@PathVariable int month,
			@PathVariable int day,
			@PageableDefault(10) Pageable pageable,
			BlogLanguage blogLanguage,
			Model model) {
		ArticleSearchForm form = new ArticleSearchForm() {};
		form.setLanguage(blogLanguage.getLanguage());
		form.setDateFrom(new LocalDateTime(year, month, day, 0, 0, 0));
		form.setDateTo(new LocalDateTime(year, month, day, 23, 59, 59));

		Page<Article> articles = articleService.readArticles(form.toArticleSearchRequest(), pageable);
		model.addAttribute("articles", articles);
		model.addAttribute("pageable", pageable);
		model.addAttribute("pagination", new Pagination<>(articles));
		return "article/index";
	}

	@RequestMapping("/category/**")
	public String category(
			@PageableDefault(10) Pageable pageable,
			BlogLanguage blogLanguage,
			HttpServletRequest request,
			Model model) {
		String path = extractPathFromPattern(request);
		String[] codes = path.split("/");
		String lastCode = codes[codes.length - 1];

		CategoryTree categoryTree = categoryService.readCategoryTree(blogLanguage.getLanguage());
		Category category = categoryTree.getCategoryByCode(lastCode);

		ArticleSearchForm form = new ArticleSearchForm() {};
		form.setLanguage(blogLanguage.getLanguage());
		form.getCategoryIds().add(category.getId());

		Page<Article> articles = articleService.readArticles(form.toArticleSearchRequest(), pageable);
		model.addAttribute("category", category);
		model.addAttribute("articles", articles);
		model.addAttribute("pageable", pageable);
		model.addAttribute("pagination", new Pagination<>(articles));
		return "article/index";
	}

	private String extractPathFromPattern(final HttpServletRequest request){
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		String bestMatchPattern = (String ) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

		AntPathMatcher apm = new AntPathMatcher();
		String finalPath = apm.extractPathWithinPattern(bestMatchPattern, path);

		return finalPath;
	}

	@RequestMapping("/tag/{name}")
	public String tag(
			@PathVariable String name,
			@PageableDefault(10) Pageable pageable,
			BlogLanguage blogLanguage,
			Model model) {
		Tag tag = tagService.readTagByName(name, blogLanguage.getLanguage());
		if (tag == null) {
			throw new HttpNotFoundException();
		}

		ArticleSearchRequest request = new ArticleSearchRequest()
				.withLanguage(blogLanguage.getLanguage())
				.withTagNames(name);
		Page<Article> articles = articleService.readArticles(request, pageable);
		model.addAttribute("tag", tag);
		model.addAttribute("articles", articles);
		model.addAttribute("pageable", pageable);
		model.addAttribute("pagination", new Pagination<>(articles));
		return "article/index";
	}

	@RequestMapping("/author/{loginId}")
	public String author(
			@PathVariable String loginId,
			@PageableDefault(10) Pageable pageable,
			BlogLanguage blogLanguage,
			HttpServletRequest request,
			Model model) {
		User author = userService.readUserByLoginId(loginId);
		if (author == null) {
			throw new HttpNotFoundException();
		}

		ArticleSearchForm form = new ArticleSearchForm() {};
		form.setLanguage(blogLanguage.getLanguage());
		form.setAuthorId(author.getId());

		Page<Article> articles = articleService.readArticles(form.toArticleSearchRequest(), pageable);
		model.addAttribute("author", author);
		model.addAttribute("articles", articles);
		model.addAttribute("pageable", pageable);
		model.addAttribute("pagination", new Pagination<>(articles));
		return "article/author";
	}

}
