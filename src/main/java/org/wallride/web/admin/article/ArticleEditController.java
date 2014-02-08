package org.wallride.web.admin.article;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.Article;
import org.wallride.core.domain.CategoryTree;
import org.wallride.core.domain.Post;
import org.wallride.core.service.ArticleService;
import org.wallride.core.service.CategoryService;
import org.wallride.core.support.AuthorizedUser;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.groups.Default;

@Controller @Lazy
@RequestMapping("/{language}/articles/edit")
public class ArticleEditController {
	
	private static Logger logger = LoggerFactory.getLogger(ArticleEditController.class); 
	
	@Inject
	private ArticleService articleService;

	@Inject
	private CategoryService categoryService;

	@ModelAttribute("article")
	public Article article(
			@PathVariable String language,
			@RequestParam long id) {
		return articleService.readArticleById(id, language);
	}

	@ModelAttribute("categoryTree")
	public CategoryTree categoryTree(@PathVariable String language) {
		return categoryService.readCategoryTree(language);
	}

	@RequestMapping(method=RequestMethod.GET)
	public String edit(
			@PathVariable String language,
			@RequestParam long id,
			Model model,
			RedirectAttributes redirectAttributes) {
		Article article = (Article) model.asMap().get("article");
		if (!language.equals(article.getLanguage())) {
			redirectAttributes.addAttribute("language", language);
			return "redirect:/_admin/{language}/articles/index";
		}

		ArticleEditForm form = ArticleEditForm.fromDomainObject(article);
		model.addAttribute("form", form);
		return "/article/edit";
	}

	@RequestMapping(method=RequestMethod.POST, params="draft")
	public String draft(
			@PathVariable String language,
			@Validated @ModelAttribute("form") ArticleEditForm form,
			BindingResult errors,
			AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes) {
		if (errors.hasErrors()) {
			for (ObjectError error : errors.getAllErrors()) {
				if (!"validation.NotNull".equals(error.getCode())) {
					return "/article/edit";
				}
			}
		}

		Article article = null;
		try {
			article = articleService.updateArticle(form.buildArticleUpdateRequest(), errors, Post.Status.DRAFT, authorizedUser);
		}
		catch (BindException e) {
			if (errors.hasErrors()) {
				logger.debug("Errors: {}", errors);
				return "/article/edit";
			}
			throw new RuntimeException(e);
		}

		redirectAttributes.addFlashAttribute("savedArticle", article);
		redirectAttributes.addAttribute("language", language);
		redirectAttributes.addAttribute("id", article.getId());
		return "redirect:/_admin/{language}/articles/describe?id={id}";
	}

	@RequestMapping(method=RequestMethod.POST, params="publish")
	public String publish(
			@PathVariable String language,
			@Validated({Default.class, ArticleEditForm.GroupPublish.class}) @ModelAttribute("form") ArticleEditForm form,
			BindingResult errors,
			AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes) {
		if (errors.hasErrors()) {
			return "/article/edit";
		}

		Article article = null;
		try {
			article = articleService.updateArticle(form.buildArticleUpdateRequest(), errors, Post.Status.PUBLISHED, authorizedUser);
		}
		catch (BindException e) {
			if (errors.hasErrors()) {
				logger.debug("Errors: {}", errors);
				return "/article/edit";
			}
			throw new RuntimeException(e);
		}

		redirectAttributes.addFlashAttribute("savedArticle", article);
		redirectAttributes.addAttribute("language", language);
		redirectAttributes.addAttribute("id", article.getId());
		return "redirect:/_admin/{language}/articles/describe?id={id}";
	}

	@RequestMapping(method=RequestMethod.POST, params="cancel")
	public String cancel(
			@Valid @ModelAttribute("form") ArticleEditForm form,
			RedirectAttributes redirectAttributes) {
		redirectAttributes.addAttribute("id", form.getId());
		return "redirect:/_admin/articles/describe/{id}";
	}
}