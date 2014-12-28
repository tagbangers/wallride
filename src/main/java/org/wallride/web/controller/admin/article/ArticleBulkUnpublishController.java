package org.wallride.web.controller.admin.article;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.Article;
import org.wallride.core.service.ArticleService;
import org.wallride.core.service.ServiceException;
import org.wallride.core.support.AuthorizedUser;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.Collection;

@Controller
@RequestMapping(value="/{language}/articles/bulk-unpublish", method=RequestMethod.POST)
public class ArticleBulkUnpublishController {

	@Inject
	private ArticleService articleService;

	private static Logger logger = LoggerFactory.getLogger(ArticleBulkUnpublishController.class);

	@RequestMapping
	public String unpublish(
			@Valid @ModelAttribute("form") ArticleBulkUnpublishForm form,
			BindingResult errors,
			AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes,
			Model model) {
		if (errors.hasErrors()) {
			logger.debug("Errors: {}", errors);
			return "redirect:/_admin/{language}/articles/index";
		}

		Collection<Article> unpublishedArticles;
		try {
			unpublishedArticles = articleService.bulkUnpublishArticle(form.toArticleBulkUnpublishRequest(), authorizedUser);
		} catch (ServiceException e) {
			return "redirect:/_admin/{language}/articles/index";
		}

		redirectAttributes.addFlashAttribute("unpublishedArticles", unpublishedArticles);
		return "redirect:/_admin/{language}/articles/index";
	}
}
