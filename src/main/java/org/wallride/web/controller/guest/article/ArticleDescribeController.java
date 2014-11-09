package org.wallride.web.controller.guest.article;

import org.joda.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.Article;
import org.wallride.core.domain.BlogLanguage;
import org.wallride.core.service.ArticleService;
import org.wallride.web.support.HttpNotFoundException;

import javax.inject.Inject;

@Controller
@RequestMapping("/{year:[0-9]{4}}/{month:[0-9]{2}}/{day:[0-9]{2}}/{code:.+}")
public class ArticleDescribeController {

	@Inject
	private ArticleService articleService;

	@RequestMapping
	public String describe(
			@PathVariable int year,
			@PathVariable int month,
			@PathVariable int day,
			@PathVariable String code,
			BlogLanguage blogLanguage,
			Model model,
			RedirectAttributes redirectAttributes) {
		Article article = articleService.readArticleByCode(code, blogLanguage.getLanguage());
		if (article == null) {
			throw new HttpNotFoundException();
		}

		LocalDate date = new LocalDate(year, month, day);
		if (!article.getDate().toLocalDate().equals(date)) {
			redirectAttributes.addAttribute("language", blogLanguage.getLanguage());
			redirectAttributes.addAttribute("year", article.getDate().getYear());
			redirectAttributes.addAttribute("month", article.getDate().getMonthOfYear());
			redirectAttributes.addAttribute("day", article.getDate().getDayOfMonth());
			redirectAttributes.addAttribute("code", code);
			return "redirect:/{language}/{year}/{month}/{day}/{code}";
		}

		model.addAttribute("article", article);
		return "article/describe";
	}
}
