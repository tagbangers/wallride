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
@RequestMapping(value="/{language}/articles/bulk-publish", method=RequestMethod.POST)
public class ArticleBulkPublishController {

	@Inject
	private ArticleService articleService;
	
	private static Logger logger = LoggerFactory.getLogger(ArticleBulkPublishController.class);

	@RequestMapping
	public String publish(
			@Valid @ModelAttribute("form") ArticleBulkPublishForm form,
			BindingResult errors,
			AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes,
			Model model) {
		if (errors.hasErrors()) {
			logger.debug("Errors: {}", errors);
			return "redirect:/_admin/{language}/articles/index";
		}
		
		Collection<Article> publishedArticles;
		try {
			publishedArticles = articleService.bulkPublishArticle(form.toArticleBulkPublishRequest(), authorizedUser);
		} catch (ServiceException e) {
			return "redirect:/_admin/{language}/articles/index";
		}

		redirectAttributes.addFlashAttribute("publishedArticles", publishedArticles);
		return "redirect:/_admin/{language}/articles/index";
	}
}
