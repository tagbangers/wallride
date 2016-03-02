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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.CustomField;
import org.wallride.core.exception.DuplicateCodeException;
import org.wallride.core.exception.EmptyCodeException;
import org.wallride.core.service.CustomFieldService;
import org.wallride.core.support.AuthorizedUser;
import org.wallride.web.support.RestValidationErrorModel;

import javax.inject.Inject;

@Controller
@RequestMapping("/{language}/customfields/edit")
public class CustomFieldEditController {
	
	private static Logger logger = LoggerFactory.getLogger(CustomFieldEditController.class);

	@Inject
	private CustomFieldService customFieldService;

	@Inject
	private MessageSourceAccessor messageSourceAccessor;

	@ModelAttribute("fieldTypes")
	public CustomField.FieldType[] setFieldTypes() {
		return CustomField.FieldType.values();
	}

	@ModelAttribute("customField")
	public CustomField setupCustomField(
			@PathVariable String language,
			@RequestParam long id) {
		return customFieldService.getCustomFieldById(id, language);
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

	@RequestMapping(method= RequestMethod.GET)
	public String edit(
			@PathVariable String language,
			@RequestParam long id,
			Model model,
			RedirectAttributes redirectAttributes) {
		CustomField customField = (CustomField) model.asMap().get("customField");
		if (!language.equals(customField.getLanguage())) {
			redirectAttributes.addAttribute("language", language);
			return "redirect:/_admin/{language}/customfields/index";
		}

		CustomFieldEditForm form = CustomFieldEditForm.fromDomainObject(customField);
		model.addAttribute("form", form);

		return "customfield/edit";
	}


	@RequestMapping(method=RequestMethod.POST)
	public String update(
			@PathVariable String language,
			@Validated @ModelAttribute("form") CustomFieldEditForm form,
			BindingResult errors,
			String query,
			AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes) {
		if (errors.hasErrors()) {
			return "customField/edit";
		}

		CustomField customField = null;
		try {
			customField = customFieldService.updateCustomField(form.buildCustomFieldUpdateRequest(), authorizedUser);
		}
		catch (EmptyCodeException e) {
			errors.rejectValue("code", "NotNull");
		}
		catch (DuplicateCodeException e) {
			errors.rejectValue("code", "NotDuplicate");
		}
		if (errors.hasErrors()) {
			logger.debug("Errors: {}", errors);
			return "customfield/edit";
		}

		redirectAttributes.addFlashAttribute("savedCustomField", customField);
		redirectAttributes.addAttribute("language", language);
		redirectAttributes.addAttribute("id", customField.getId());
		return "redirect:/_admin/{language}/customfields/index";
	}
}