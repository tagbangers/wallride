package org.wallride.web.controller.guest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.wallride.core.domain.Article;
import org.wallride.core.domain.BlogLanguage;
import org.wallride.core.service.ArticleService;
import org.wallride.core.support.Pagination;
import org.wallride.web.controller.guest.article.ArticleSearchForm;

import javax.inject.Inject;

@Controller
@RequestMapping("/")
public class IndexController {

	@Inject
	private ArticleService articleService;

	@RequestMapping
	public String index(
			@PageableDefault(10) Pageable pageable,
			BlogLanguage blogLanguage,
			Model model) {
		ArticleSearchForm form = new ArticleSearchForm() {};
		form.setLanguage(blogLanguage.getLanguage());

		Page<Article> articles = articleService.readArticles(form.buildArticleSearchRequest(), pageable);
		model.addAttribute("articles", articles);
		model.addAttribute("pageable", pageable);
		model.addAttribute("pagination", new Pagination<>(articles));
		return "article/index";
//
//
//		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);
//		String defaultLanguage = blog.getDefaultLanguage();
//		redirectAttributes.addAttribute("language", defaultLanguage);
//		return "redirect:/{language}/";
	}
}
