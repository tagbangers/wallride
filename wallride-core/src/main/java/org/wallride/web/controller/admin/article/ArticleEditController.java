/*
 * Copyright 2014 Tagbangers, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wallride.web.controller.admin.article;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.domain.CustomField;
import org.wallride.service.CustomFieldService;
import org.wallride.domain.Article;
import org.wallride.domain.Category;
import org.wallride.exception.DuplicateCodeException;
import org.wallride.exception.EmptyCodeException;
import org.wallride.model.TreeNode;
import org.wallride.service.ArticleService;
import org.wallride.support.AuthorizedUser;
import org.wallride.support.CategoryUtils;
import org.wallride.web.support.DomainObjectSavedModel;
import org.wallride.web.support.HttpNotFoundException;
import org.wallride.web.support.RestValidationErrorModel;

import javax.inject.Inject;
import javax.validation.groups.Default;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

@Controller
@RequestMapping("/{language}/articles/edit")
public class ArticleEditController {
	
	private static Logger logger = LoggerFactory.getLogger(ArticleEditController.class); 
	
	@Inject
	private ArticleService articleService;

	@Inject
	private CustomFieldService customFieldService;

	@Inject
	private CategoryUtils categoryUtils;

	@Inject
	private MessageSourceAccessor messageSourceAccessor;

	@ModelAttribute("article")
	public Article setupArticle(@RequestParam long id) {
		return articleService.getArticleById(id);
	}

	@ModelAttribute("categoryNodes")
	public List<TreeNode<Category>> setupCategoryNodes(@PathVariable String language) {
		return categoryUtils.getNodes(true);
	}

	@ModelAttribute("query")
	public String query(@RequestParam(required = false) String query) {
		return query;
	}

	@ExceptionHandler(BindException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public @ResponseBody
	RestValidationErrorModel bindException(BindException e) {
		logger.debug("BindException", e);
		return RestValidationErrorModel.fromBindingResult(e.getBindingResult(), messageSourceAccessor);
	}

	@RequestMapping(method=RequestMethod.GET)
	public String edit(
			@PathVariable String language,
			@RequestParam long id,
			Model model,
			RedirectAttributes redirectAttributes) {
		Article article = (Article) model.asMap().get("article");
		if (article == null) {
			throw new HttpNotFoundException();
		}

		if (!article.getLanguage().equals(language)) {
			Article target = articleService.getArticleByCode(article.getCode(), language);
			if (target != null) {
				redirectAttributes.addAttribute("id", target.getId());
				return "redirect:/_admin/{language}/articles/edit?id={id}";
			} else {
				redirectAttributes.addFlashAttribute("original", article);
				redirectAttributes.addAttribute("code", article.getCode());
				return "redirect:/_admin/{language}/articles/create?code={code}";
			}
		}

		Set<CustomField> customFields = customFieldService.getAllCustomFields(language);
		ArticleEditForm form = ArticleEditForm.fromDomainObject(article, customFields);
		model.addAttribute("form", form);

		Article draft = articleService.getDraftById(id);
		model.addAttribute("draft", draft);

		return "article/edit";
	}

	@RequestMapping(method=RequestMethod.GET, params="draft")
	public String editDraft(
			@PathVariable String language,
			@RequestParam long id,
			String query,
			Model model,
			RedirectAttributes redirectAttributes) {
		Article article = (Article) model.asMap().get("article");
		if (!language.equals(article.getLanguage())) {
			redirectAttributes.addAttribute("language", language);
			redirectAttributes.addAttribute("query", query);
			return "redirect:/_admin/{language}/articles/index";
		}

		Article draft = articleService.getDraftById(id);
		if (draft == null) {
			redirectAttributes.addAttribute("language", language);
			redirectAttributes.addAttribute("id", id);
			redirectAttributes.addAttribute("query", query);
			return "redirect:/_admin/{language}/articles/edit";
		}
		SortedSet<CustomField> customFields = customFieldService.getAllCustomFields(language);
		ArticleEditForm form = ArticleEditForm.fromDomainObject(draft, customFields);
		model.addAttribute("form", form);

		return "article/edit";
	}

	@RequestMapping(method=RequestMethod.POST, params="draft")
	public @ResponseBody DomainObjectSavedModel saveAsDraft(
			@PathVariable String language,
			@Validated @ModelAttribute("form") ArticleEditForm form,
			BindingResult errors,
			Model model,
			AuthorizedUser authorizedUser)
			throws BindException {
		if (errors.hasErrors()) {
			for (ObjectError error : errors.getAllErrors()) {
				if (!"validation.NotNull".equals(error.getCode())) {
					throw new BindException(errors);
				}
			}
		}

		Article article = (Article) model.asMap().get("article");
		try {
			articleService.saveArticleAsDraft(form.buildArticleUpdateRequest(), authorizedUser);
		}
		catch (EmptyCodeException e) {
			errors.rejectValue("code", "NotNull");
		}
		catch (DuplicateCodeException e) {
			errors.rejectValue("code", "NotDuplicate");
		}
		if (errors.hasErrors()) {
			logger.debug("Errors: {}", errors);
			throw new BindException(errors);
		}

		return new DomainObjectSavedModel<>(article);
	}

	@RequestMapping(method=RequestMethod.POST, params="publish")
	public String saveAsPublished(
			@PathVariable String language,
			@Validated({Default.class, ArticleEditForm.GroupPublish.class}) @ModelAttribute("form") ArticleEditForm form,
			BindingResult errors,
			String query,
			AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes) {
		if (errors.hasErrors()) {
			return "article/edit";
		}

		Article article = null;
		try {
			article = articleService.saveArticleAsPublished(form.buildArticleUpdateRequest(), authorizedUser);
		}
		catch (EmptyCodeException e) {
			errors.rejectValue("code", "NotNull");
		}
		catch (DuplicateCodeException e) {
			errors.rejectValue("code", "NotDuplicate");
		}
		if (errors.hasErrors()) {
			logger.debug("Errors: {}", errors);
			return "article/edit";
		}

		redirectAttributes.addFlashAttribute("savedArticle", article);
		redirectAttributes.addAttribute("language", language);
		redirectAttributes.addAttribute("id", article.getId());
		redirectAttributes.addAttribute("query", query);
		return "redirect:/_admin/{language}/articles/describe";
	}

	@RequestMapping(method=RequestMethod.POST, params="unpublish")
	public String saveAsUnpublished(
			@PathVariable String language,
			@Validated({Default.class, ArticleEditForm.GroupPublish.class}) @ModelAttribute("form") ArticleEditForm form,
			BindingResult errors,
			String query,
			AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes) {
		if (errors.hasErrors()) {
			return "article/edit";
		}

		Article article = null;
		try {
			article = articleService.saveArticleAsUnpublished(form.buildArticleUpdateRequest(), authorizedUser);
		}
		catch (EmptyCodeException e) {
			errors.rejectValue("code", "NotNull");
		}
		catch (DuplicateCodeException e) {
			errors.rejectValue("code", "NotDuplicate");
		}
		if (errors.hasErrors()) {
			logger.debug("Errors: {}", errors);
			return "article/edit";
		}

		redirectAttributes.addFlashAttribute("savedArticle", article);
		redirectAttributes.addAttribute("language", language);
		redirectAttributes.addAttribute("id", article.getId());
		redirectAttributes.addAttribute("query", query);
		return "redirect:/_admin/{language}/articles/describe";
	}

	@RequestMapping(method=RequestMethod.POST, params="update")
	public String update(
			@PathVariable String language,
			@Validated({Default.class, ArticleEditForm.GroupPublish.class}) @ModelAttribute("form") ArticleEditForm form,
			BindingResult errors,
			String query,
			AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes) {
		if (errors.hasErrors()) {
			return "article/edit";
		}

		Article article = null;
		try {
			article = articleService.saveArticle(form.buildArticleUpdateRequest(), authorizedUser);
		}
		catch (EmptyCodeException e) {
			errors.rejectValue("code", "NotNull");
		}
		catch (DuplicateCodeException e) {
			errors.rejectValue("code", "NotDuplicate");
		}
		if (errors.hasErrors()) {
			logger.debug("Errors: {}", errors);
			return "article/edit";
		}

		redirectAttributes.addFlashAttribute("savedArticle", article);
		redirectAttributes.addAttribute("language", language);
		redirectAttributes.addAttribute("id", article.getId());
		redirectAttributes.addAttribute("query", query);
		return "redirect:/_admin/{language}/articles/describe";
	}

//	@RequestMapping(method=RequestMethod.POST, params="cancel")
//	public String cancel(
//			@Valid @ModelAttribute("form") ArticleEditForm form,
//			RedirectAttributes redirectAttributes) {
//		redirectAttributes.addAttribute("id", form.getId());
//		return "redirect:/_admin/articles/describe/{id}";
//	}
}