package org.wallride.web.controller.guest.article;

import org.joda.time.LocalDateTime;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.HandlerMapping;
import org.wallride.core.domain.Article;
import org.wallride.core.domain.Category;
import org.wallride.core.domain.CategoryTree;
import org.wallride.core.service.ArticleService;
import org.wallride.core.service.CategoryService;
import org.wallride.core.support.Paginator;
import org.wallride.web.support.DomainObjectSearchCondition;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class ArticleIndexController {

//	/article/[:yyyy]/[:mm]/[:dd]/[:code]
//	/categories/[:code]/[:code]/[:code]/[:code]/
//	/tag/[:code]/

	@Inject
	private ArticleService articleService;

	@Inject
	private CategoryService categoryService;

	@RequestMapping("/{language}/")
	public String index(
			@PathVariable String language,
			@RequestParam(required=false) Integer page,
			@RequestParam(required=false) String token,
			HttpSession session,
			Model model) {
		DomainObjectSearchCondition<ArticleSearchForm> condition = DomainObjectSearchCondition.resolve(session, ArticleSearchForm.class, token);
		if (condition == null) {
			ArticleSearchForm form = new ArticleSearchForm() {};
			form.setLanguage(language);
			List<Long> ids = articleService.searchArticles(form.buildArticleSearchRequest());
			Paginator<Long> paginator = new Paginator<>(ids, 20);
			condition = new DomainObjectSearchCondition<ArticleSearchForm>(session, form, paginator);
		}
		if (page != null && condition.getPaginator().hasElement()) {
			condition.getPaginator().setNumber(page);
		}


		List<Article> articles = articleService.readArticles(condition.getPaginator());
		model.addAttribute("articles", articles);
		model.addAttribute("paginator", condition.getPaginator());
		return "/article/index";
	}

	@RequestMapping("/{language}/{year:[0-9]{4}}")
	public String year(
			@PathVariable String language,
			@PathVariable int year,
			@RequestParam(required=false) Integer page,
			@RequestParam(required=false) String token,
			HttpSession session,
			Model model) {
		DomainObjectSearchCondition<ArticleSearchForm> condition = DomainObjectSearchCondition.resolve(session, ArticleSearchForm.class, token);
		if (condition == null) {
			ArticleSearchForm form = new ArticleSearchForm() {};
			form.setLanguage(language);
			form.setDateFrom(new LocalDateTime(year, 1, 1, 0, 0, 0));
			form.setDateTo(new LocalDateTime(year, 12, 31, 0, 0, 0));
			List<Long> ids = articleService.searchArticles(form.buildArticleSearchRequest());
			Paginator<Long> paginator = new Paginator<>(ids, 20);
			condition = new DomainObjectSearchCondition<ArticleSearchForm>(session, form, paginator);
		}
		if (page != null && condition.getPaginator().hasElement()) {
			condition.getPaginator().setNumber(page);
		}

		List<Article> articles = articleService.readArticles(condition.getPaginator());
		model.addAttribute("articles", articles);
		model.addAttribute("paginator", condition.getPaginator());
		return "/article/index";
	}

	@RequestMapping("/{language}/{year:[0-9]{4}}/{month:[0-9]{2}}")
	public String month(
			@PathVariable String language,
			@PathVariable int year,
			@PathVariable int month,
			@RequestParam(required=false) Integer page,
			@RequestParam(required=false) String token,
			HttpSession session,
			Model model) {
		DomainObjectSearchCondition<ArticleSearchForm> condition = DomainObjectSearchCondition.resolve(session, ArticleSearchForm.class, token);
		if (condition == null) {
			ArticleSearchForm form = new ArticleSearchForm() {};
			form.setLanguage(language);
			LocalDateTime date = new LocalDateTime(year, month, 1, 0, 0, 0);
			form.setDateFrom(new LocalDateTime(year, month, 1, 0, 0, 0));
			form.setDateTo(new LocalDateTime(year, month, date.dayOfMonth().getMaximumValue(), 23, 59, 59));
			List<Long> ids = articleService.searchArticles(form.buildArticleSearchRequest());
			Paginator<Long> paginator = new Paginator<>(ids, 20);
			condition = new DomainObjectSearchCondition<ArticleSearchForm>(session, form, paginator);
		}
		if (page != null && condition.getPaginator().hasElement()) {
			condition.getPaginator().setNumber(page);
		}

		List<Article> articles = articleService.readArticles(condition.getPaginator());
		model.addAttribute("articles", articles);
		model.addAttribute("paginator", condition.getPaginator());
		return "/article/index";
	}

	@RequestMapping("/{language}/{year:[0-9]{4}}/{month:[0-9]{2}}/{day:[0-9]{2}}")
	public String day(
			@PathVariable String language,
			@PathVariable int year,
			@PathVariable int month,
			@PathVariable int day,
			@RequestParam(required=false) Integer page,
			@RequestParam(required=false) String token,
			HttpSession session,
			Model model) {
		DomainObjectSearchCondition<ArticleSearchForm> condition = DomainObjectSearchCondition.resolve(session, ArticleSearchForm.class, token);
		if (condition == null) {
			ArticleSearchForm form = new ArticleSearchForm() {};
			form.setLanguage(language);
			form.setDateFrom(new LocalDateTime(year, month, day, 0, 0, 0));
			form.setDateTo(new LocalDateTime(year, month, day, 23, 59, 59));
			List<Long> ids = articleService.searchArticles(form.buildArticleSearchRequest());
			Paginator<Long> paginator = new Paginator<>(ids, 20);
			condition = new DomainObjectSearchCondition<ArticleSearchForm>(session, form, paginator);
		}
		if (page != null && condition.getPaginator().hasElement()) {
			condition.getPaginator().setNumber(page);
		}

		List<Article> articles = articleService.readArticles(condition.getPaginator());
		model.addAttribute("articles", articles);
		model.addAttribute("paginator", condition.getPaginator());
		return "/article/index";
	}

	@RequestMapping("/{language}/category/**")
	public String category(
			@PathVariable String language,
			@RequestParam(required=false) Integer page,
			@RequestParam(required=false) String token,
			HttpServletRequest request,
			HttpSession session,
			Model model) {
		String path = extractPathFromPattern(request);
		String[] codes = path.split("/");
		String lastCode = codes[codes.length - 1];

		CategoryTree categoryTree = categoryService.readCategoryTree(language);
		Category category = categoryTree.getCategoryByCode(lastCode);

		DomainObjectSearchCondition<ArticleSearchForm> condition = DomainObjectSearchCondition.resolve(session, ArticleSearchForm.class, token);
		if (condition == null) {
			ArticleSearchForm form = new ArticleSearchForm() {};
			form.setLanguage(language);
			form.getCategoryIds().add(category.getId());
			List<Long> ids = articleService.searchArticles(form.buildArticleSearchRequest());
			Paginator<Long> paginator = new Paginator<>(ids, 20);
			condition = new DomainObjectSearchCondition<ArticleSearchForm>(session, form, paginator);
		}
		if (page != null && condition.getPaginator().hasElement()) {
			condition.getPaginator().setNumber(page);
		}

		List<Article> articles = articleService.readArticles(condition.getPaginator());
		model.addAttribute("category", category);
		model.addAttribute("articles", articles);
		model.addAttribute("paginator", condition.getPaginator());
		return "/article/index";
	}

	private String extractPathFromPattern(final HttpServletRequest request){
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		String bestMatchPattern = (String ) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

		AntPathMatcher apm = new AntPathMatcher();
		String finalPath = apm.extractPathWithinPattern(bestMatchPattern, path);

		return finalPath;
	}
}
