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

package org.wallride.web.controller.admin.page;

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
import org.wallride.core.domain.Category;
import org.wallride.core.domain.CustomField;
import org.wallride.core.domain.Page;
import org.wallride.core.exception.DuplicateCodeException;
import org.wallride.core.exception.EmptyCodeException;
import org.wallride.core.model.TreeNode;
import org.wallride.core.service.CustomFieldService;
import org.wallride.core.service.PageService;
import org.wallride.core.support.AuthorizedUser;
import org.wallride.core.support.CategoryUtils;
import org.wallride.web.controller.admin.article.ArticleEditForm;
import org.wallride.web.support.DomainObjectSavedModel;
import org.wallride.web.support.RestValidationErrorModel;

import javax.inject.Inject;
import javax.validation.groups.Default;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/{language}/pages/edit")
public class PageEditController {
	
	private static Logger logger = LoggerFactory.getLogger(PageEditController.class); 
	
	@Inject
	private PageService pageService;

	@Inject
	private CustomFieldService customFieldService;

	@Inject
	private CategoryUtils categoryUtils;

	@Inject
	private MessageSourceAccessor messageSourceAccessor;

	@ModelAttribute("page")
	public Page setupPage(
			@PathVariable String language,
			@RequestParam long id) {
		return pageService.getPageById(id, language);
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
	public @ResponseBody RestValidationErrorModel bindException(BindException e) {
		logger.debug("BindException", e);
		return RestValidationErrorModel.fromBindingResult(e.getBindingResult(), messageSourceAccessor);
	}

	@RequestMapping(method=RequestMethod.GET)
	public String edit(
			@PathVariable String language,
			@RequestParam long id,
			Model model,
			RedirectAttributes redirectAttributes) {
		Page page = pageService.getPageById(id, language);
		if (!language.equals(page.getLanguage())) {
			redirectAttributes.addAttribute("language", language);
			return "redirect:/_admin/{language}/pages/index";
		}
		Set<CustomField> customFields = customFieldService.getAllCustomFields();
		PageEditForm form = PageEditForm.fromDomainObject(page, customFields);
		model.addAttribute("form", form);

		Page draft = pageService.getDraftById(id);
		model.addAttribute("draft", draft);

		return "page/edit";
	}

	@RequestMapping(method=RequestMethod.GET, params="draft")
	public String editDraft(
			@PathVariable String language,
			@RequestParam long id,
			String query,
			Model model,
			RedirectAttributes redirectAttributes) {
		Page page = (Page) model.asMap().get("page");
		if (!language.equals(page.getLanguage())) {
			redirectAttributes.addAttribute("language", language);
			redirectAttributes.addAttribute("query", query);
			return "redirect:/_admin/{language}/pages/index";
		}
		Page draft = pageService.getDraftById(id);
		if (draft == null) {
			redirectAttributes.addAttribute("language", language);
			redirectAttributes.addAttribute("id", id);
			redirectAttributes.addAttribute("query", query);
			return "redirect:/_admin/{language}/pages/edit";
		}
		Set<CustomField> customFields = customFieldService.getAllCustomFields();
		PageEditForm form = PageEditForm.fromDomainObject(draft, customFields);
		model.addAttribute("form", form);

		return "page/edit";
	}

	@RequestMapping(method=RequestMethod.POST, params="draft")
	public @ResponseBody DomainObjectSavedModel saveAsDraft(
			@PathVariable String language,
			@Validated @ModelAttribute("form") PageEditForm form,
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

		Page page = (Page) model.asMap().get("page");
		try {
			pageService.savePageAsDraft(form.buildPageUpdateRequest(), authorizedUser);
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

		return new DomainObjectSavedModel<>(page);
	}
	
	@RequestMapping(method=RequestMethod.POST, params="publish")
	public String saveAsPublished(
			@PathVariable String language,
			@Validated({Default.class, PageEditForm.GroupPublish.class}) @ModelAttribute("form") PageEditForm form,
			BindingResult errors,
			String query,
			AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes) {
		if (errors.hasErrors()) {
			return "page/edit";
		}

		Page page = null;
		try {
			page = pageService.savePageAsPublished(form.buildPageUpdateRequest(), authorizedUser);
		}
		catch (EmptyCodeException e) {
			errors.rejectValue("code", "NotNull");
		}
		catch (DuplicateCodeException e) {
			errors.rejectValue("code", "NotDuplicate");
		}
		if (errors.hasErrors()) {
			logger.debug("Errors: {}", errors);
			return "page/edit";
		}

		redirectAttributes.addFlashAttribute("savedPage", page);
		redirectAttributes.addAttribute("language", language);
		redirectAttributes.addAttribute("id", page.getId());
		redirectAttributes.addAttribute("query", query);
		return "redirect:/_admin/{language}/pages/describe";
	}

	@RequestMapping(method=RequestMethod.POST, params="unpublish")
	public String saveAsUnpublished(
			@PathVariable String language,
			@Validated({Default.class, PageEditForm.GroupPublish.class}) @ModelAttribute("form") PageEditForm form,
			BindingResult errors,
			String query,
			AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes) {
		if (errors.hasErrors()) {
			return "page/edit";
		}

		Page page = null;
		try {
			page = pageService.savePageAsUnpublished(form.buildPageUpdateRequest(), authorizedUser);
		}
		catch (EmptyCodeException e) {
			errors.rejectValue("code", "NotNull");
		}
		catch (DuplicateCodeException e) {
			errors.rejectValue("code", "NotDuplicate");
		}
		if (errors.hasErrors()) {
			logger.debug("Errors: {}", errors);
			return "page/edit";
		}

		redirectAttributes.addFlashAttribute("savedPage", page);
		redirectAttributes.addAttribute("language", language);
		redirectAttributes.addAttribute("id", page.getId());
		redirectAttributes.addAttribute("query", query);
		return "redirect:/_admin/{language}/pages/describe";
	}

	@RequestMapping(method=RequestMethod.POST, params="update")
	public String update(
			@PathVariable String language,
			@Validated({Default.class, PageEditForm.GroupPublish.class}) @ModelAttribute("form") PageEditForm form,
			BindingResult errors,
			String query,
			AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes) {
		if (errors.hasErrors()) {
			return "page/edit";
		}

		Page page = null;
		try {
			page = pageService.savePage(form.buildPageUpdateRequest(), authorizedUser);
		}
		catch (EmptyCodeException e) {
			errors.rejectValue("code", "NotNull");
		}
		catch (DuplicateCodeException e) {
			errors.rejectValue("code", "NotDuplicate");
		}
		if (errors.hasErrors()) {
			logger.debug("Errors: {}", errors);
			return "page/edit";
		}

		redirectAttributes.addFlashAttribute("savedPage", page);
		redirectAttributes.addAttribute("language", language);
		redirectAttributes.addAttribute("id", page.getId());
		redirectAttributes.addAttribute("query", query);
		return "redirect:/_admin/{language}/pages/describe";
	}
	
//	@RequestMapping(method=RequestMethod.POST, params="cancel")
//	public String cancel(
//			@Valid @ModelAttribute("form") PageEditForm form,
//			RedirectAttributes redirectAttributes) {
//		redirectAttributes.addAttribute("id", form.getId());
//		return "redirect:/_admin/pages/describe/{id}";
//	}
}