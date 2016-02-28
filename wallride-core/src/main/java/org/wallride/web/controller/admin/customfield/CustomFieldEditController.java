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

package org.wallride.web.controller.admin.customfield;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/{language}/customFields/edit")
public class CustomFieldEditController {
	
//	private static Logger logger = LoggerFactory.getLogger(CustomFieldEditController.class);
//
//	@Inject
//	private CustomFieldService customFieldService;
//
//	@Inject
//	private CategoryUtils categoryUtils;
//
//	@Inject
//	private MessageSourceAccessor messageSourceAccessor;
//
//	@ModelAttribute("customField")
//	public CustomField setupCustomField(
//			@PathVariable String language,
//			@RequestParam long id) {
//		return customFieldService.getCustomFieldById(id, language);
//	}
//
//	@ModelAttribute("categoryNodes")
//	public List<TreeNode<Category>> setupCategoryNodes(@PathVariable String language) {
//		return categoryUtils.getNodes(true);
//	}
//
//	@ModelAttribute("query")
//	public String query(@RequestParam(required = false) String query) {
//		return query;
//	}
//
//	@ExceptionHandler(BindException.class)
//	@ResponseStatus(HttpStatus.BAD_REQUEST)
//	public @ResponseBody
//	RestValidationErrorModel bindException(BindException e) {
//		logger.debug("BindException", e);
//		return RestValidationErrorModel.fromBindingResult(e.getBindingResult(), messageSourceAccessor);
//	}
//
//	@RequestMapping(method=RequestMethod.GET)
//	public String edit(
//			@PathVariable String language,
//			@RequestParam long id,
//			Model model,
//			RedirectAttributes redirectAttributes) {
//		CustomField customField = (CustomField) model.asMap().get("customField");
//		if (!language.equals(customField.getLanguage())) {
//			redirectAttributes.addAttribute("language", language);
//			return "redirect:/_admin/{language}/customFields/index";
//		}
//
//		CustomFieldEditForm form = CustomFieldEditForm.fromDomainObject(customField);
//		model.addAttribute("form", form);
//
//		CustomField draft = customFieldService.getDraftById(id);
//		model.addAttribute("draft", draft);
//
//		return "customField/edit";
//	}
//
//	@RequestMapping(method=RequestMethod.GET, params="draft")
//	public String editDraft(
//			@PathVariable String language,
//			@RequestParam long id,
//			String query,
//			Model model,
//			RedirectAttributes redirectAttributes) {
//		CustomField customField = (CustomField) model.asMap().get("customField");
//		if (!language.equals(customField.getLanguage())) {
//			redirectAttributes.addAttribute("language", language);
//			redirectAttributes.addAttribute("query", query);
//			return "redirect:/_admin/{language}/customFields/index";
//		}
//
//		CustomField draft = customFieldService.getDraftById(id);
//		if (draft == null) {
//			redirectAttributes.addAttribute("language", language);
//			redirectAttributes.addAttribute("id", id);
//			redirectAttributes.addAttribute("query", query);
//			return "redirect:/_admin/{language}/customFields/edit";
//		}
//
//		CustomFieldEditForm form = CustomFieldEditForm.fromDomainObject(draft);
//		model.addAttribute("form", form);
//
//		return "customField/edit";
//	}
//
//	@RequestMapping(method=RequestMethod.POST, params="draft")
//	public @ResponseBody DomainObjectSavedModel saveAsDraft(
//			@PathVariable String language,
//			@Validated @ModelAttribute("form") CustomFieldEditForm form,
//			BindingResult errors,
//			Model model,
//			AuthorizedUser authorizedUser)
//			throws BindException {
//		if (errors.hasErrors()) {
//			for (ObjectError error : errors.getAllErrors()) {
//				if (!"validation.NotNull".equals(error.getCode())) {
//					throw new BindException(errors);
//				}
//			}
//		}
//
//		CustomField customField = (CustomField) model.asMap().get("customField");
//		try {
//			customFieldService.saveCustomFieldAsDraft(form.buildCustomFieldUpdateRequest(), authorizedUser);
//		}
//		catch (EmptyCodeException e) {
//			errors.rejectValue("code", "NotNull");
//		}
//		catch (DuplicateCodeException e) {
//			errors.rejectValue("code", "NotDuplicate");
//		}
//		if (errors.hasErrors()) {
//			logger.debug("Errors: {}", errors);
//			throw new BindException(errors);
//		}
//
//		return new DomainObjectSavedModel<>(customField);
//	}
//
//	@RequestMapping(method=RequestMethod.POST, params="publish")
//	public String saveAsPublished(
//			@PathVariable String language,
//			@Validated @ModelAttribute("form") CustomFieldEditForm form,
//			BindingResult errors,
//			String query,
//			AuthorizedUser authorizedUser,
//			RedirectAttributes redirectAttributes) {
//		if (errors.hasErrors()) {
//			return "customField/edit";
//		}
//
//		CustomField customField = null;
//		try {
//			customField = customFieldService.saveCustomFieldAsPublished(form.buildCustomFieldUpdateRequest(), authorizedUser);
//		}
//		catch (EmptyCodeException e) {
//			errors.rejectValue("code", "NotNull");
//		}
//		catch (DuplicateCodeException e) {
//			errors.rejectValue("code", "NotDuplicate");
//		}
//		if (errors.hasErrors()) {
//			logger.debug("Errors: {}", errors);
//			return "customField/edit";
//		}
//
//		redirectAttributes.addFlashAttribute("savedCustomField", customField);
//		redirectAttributes.addAttribute("language", language);
//		redirectAttributes.addAttribute("id", customField.getId());
//		redirectAttributes.addAttribute("query", query);
//		return "redirect:/_admin/{language}/customFields/describe";
//	}
//
//	@RequestMapping(method=RequestMethod.POST, params="unpublish")
//	public String saveAsUnpublished(
//			@PathVariable String language,
//			@Validated @ModelAttribute("form") CustomFieldEditForm form,
//			BindingResult errors,
//			String query,
//			AuthorizedUser authorizedUser,
//			RedirectAttributes redirectAttributes) {
//		if (errors.hasErrors()) {
//			return "customField/edit";
//		}
//
//		CustomField customField = null;
//		try {
//			customField = customFieldService.saveCustomFieldAsUnpublished(form.buildCustomFieldUpdateRequest(), authorizedUser);
//		}
//		catch (EmptyCodeException e) {
//			errors.rejectValue("code", "NotNull");
//		}
//		catch (DuplicateCodeException e) {
//			errors.rejectValue("code", "NotDuplicate");
//		}
//		if (errors.hasErrors()) {
//			logger.debug("Errors: {}", errors);
//			return "customField/edit";
//		}
//
//		redirectAttributes.addFlashAttribute("savedCustomField", customField);
//		redirectAttributes.addAttribute("language", language);
//		redirectAttributes.addAttribute("id", customField.getId());
//		redirectAttributes.addAttribute("query", query);
//		return "redirect:/_admin/{language}/customFields/describe";
//	}
//
//	@RequestMapping(method=RequestMethod.POST, params="update")
//	public String update(
//			@PathVariable String language,
//			@Validated @ModelAttribute("form") CustomFieldEditForm form,
//			BindingResult errors,
//			String query,
//			AuthorizedUser authorizedUser,
//			RedirectAttributes redirectAttributes) {
//		if (errors.hasErrors()) {
//			return "customField/edit";
//		}
//
//		CustomField customField = null;
//		try {
//			customField = customFieldService.saveCustomField(form.buildCustomFieldUpdateRequest(), authorizedUser);
//		}
//		catch (EmptyCodeException e) {
//			errors.rejectValue("code", "NotNull");
//		}
//		catch (DuplicateCodeException e) {
//			errors.rejectValue("code", "NotDuplicate");
//		}
//		if (errors.hasErrors()) {
//			logger.debug("Errors: {}", errors);
//			return "customField/edit";
//		}
//
//		redirectAttributes.addFlashAttribute("savedCustomField", customField);
//		redirectAttributes.addAttribute("language", language);
//		redirectAttributes.addAttribute("id", customField.getId());
//		redirectAttributes.addAttribute("query", query);
//		return "redirect:/_admin/{language}/customFields/describe";
//	}

//	@RequestMapping(method=RequestMethod.POST, params="cancel")
//	public String cancel(
//			@Valid @ModelAttribute("form") CustomFieldEditForm form,
//			RedirectAttributes redirectAttributes) {
//		redirectAttributes.addAttribute("id", form.getId());
//		return "redirect:/_admin/customFields/describe/{id}";
//	}
}