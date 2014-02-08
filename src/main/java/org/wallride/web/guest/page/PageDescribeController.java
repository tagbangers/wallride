package org.wallride.web.guest.page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.wallride.core.domain.Page;
import org.wallride.core.service.PageService;
import org.wallride.web.HttpNotFoundException;
import org.wallride.web.guest.article.ArticleIndexController;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/{language}/{code}")
public class PageDescribeController {

	@Inject
	private PageService pageService;

	@Inject
	private ArticleIndexController articleIndexController;

	@RequestMapping
	public String describe(
			@PathVariable String code,
			@PathVariable String language,
			HttpSession session,
			Model model) {
		if (code.matches("[0-9]{4}")) {
			return articleIndexController.year(language, Integer.parseInt(code), null, null, session, model);
		}

		Page page = pageService.readPage(code);
		if (page == null) {
			throw new HttpNotFoundException();
		}

		model.addAttribute("page", page);
		return "/page/describe";
	}
}
