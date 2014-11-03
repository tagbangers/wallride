package org.wallride.web.controller.admin.article;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.Article;
import org.wallride.core.service.ArticleService;

import javax.inject.Inject;
import javax.validation.Valid;

@Controller
@RequestMapping(value="/{language}/articles/delete", method=RequestMethod.POST)
public class ArticleDeleteController {
	
	private static Logger logger = LoggerFactory.getLogger(ArticleDeleteController.class); 
	
	@Inject
	private ArticleService articleService;
	
	@RequestMapping
	public String delete(
			@Valid @ModelAttribute("form") ArticleDeleteForm form,
			BindingResult errors,
			RedirectAttributes redirectAttributes) {
//		if (!form.isConfirmed()) {
//			errors.rejectValue("confirmed", "Confirmed");
//		}
		if (errors.hasErrors()) {
			logger.debug("Errors: {}", errors);
			return "article/describe";
		}

		Article article = null;
		try {
			article = articleService.deleteArticle(form.buildArticleDeleteRequest(), errors);
		}
		catch (BindException e) {
			if (errors.hasErrors()) {
				logger.debug("Errors: {}", errors);
				return "article/describe";
			}
			throw new RuntimeException(e);
		}

		redirectAttributes.addFlashAttribute("deletedArticle", article);
		return "redirect:/_admin/{language}/articles/index";
	}
}
