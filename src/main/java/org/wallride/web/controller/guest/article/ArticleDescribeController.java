package org.wallride.web.controller.guest.article;

import org.joda.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.Article;
import org.wallride.core.domain.BlogLanguage;
import org.wallride.core.domain.Comment;
import org.wallride.core.service.ArticleService;
import org.wallride.core.service.CommentSearchRequest;
import org.wallride.core.service.CommentService;
import org.wallride.web.support.HttpNotFoundException;

import javax.inject.Inject;

@Controller
@RequestMapping("/{year:[0-9]{4}}/{month:[0-9]{2}}/{day:[0-9]{2}}/{code:.+}")
public class ArticleDescribeController {

	@Inject
	private ArticleService articleService;
	@Inject
	private CommentService commentService;

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
			redirectAttributes.addAttribute("year", article.getDate().getYear());
			redirectAttributes.addAttribute("month", article.getDate().getMonthOfYear());
			redirectAttributes.addAttribute("day", article.getDate().getDayOfMonth());
			redirectAttributes.addAttribute("code", code);
			return "redirect:/{year}/{month}/{day}/{code}";
		}

		CommentSearchRequest request = new CommentSearchRequest();
		request.setPostId(article.getId());
		Page<Comment> comments = commentService.readComments(request, new PageRequest(0, 1000));

		model.addAttribute("article", article);
		model.addAttribute("comments", comments);
		return "article/describe";
	}
}
