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
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.Category;
import org.wallride.core.domain.CustomField;
import org.wallride.core.domain.Post;
import org.wallride.core.exception.DuplicateCodeException;
import org.wallride.core.exception.EmptyCodeException;
import org.wallride.core.model.TreeNode;
import org.wallride.core.service.CustomFieldService;
import org.wallride.core.support.AuthorizedUser;
import org.wallride.core.support.CategoryUtils;
import org.wallride.web.support.DomainObjectSavedModel;
import org.wallride.web.support.RestValidationErrorModel;

import javax.inject.Inject;
import javax.validation.groups.Default;
import java.util.List;

@Controller
@RequestMapping("/{language}/customfields/create")
public class CustomFieldCreateController {

	private static Logger logger = LoggerFactory.getLogger(CustomFieldCreateController.class);

	@Inject
	private CustomFieldService customfieldService;

	@Inject
	private MessageSourceAccessor messageSourceAccessor;

	@ModelAttribute("fieldTypes")
	public CustomField.FieldType[] setFieldTypes() {
		return CustomField.FieldType.values();
	}

	@ModelAttribute("form")
	public CustomFieldCreateForm customfieldCreateForm() {
		return new CustomFieldCreateForm();
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
	public String create() {
		return "customfield/create";
	}

	@RequestMapping(method=RequestMethod.POST)
	public String saveAsDraft(
			@PathVariable String language,
			@Validated @ModelAttribute("form") CustomFieldCreateForm form,
			BindingResult errors,
			String query,
			AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes)
			throws BindException {
		if (errors.hasErrors()) {
			return "customfield/create";
		}

		CustomField customfield = null;
		try {
			customfield = customfieldService.createCustomField(form.buildCustomFieldCreateRequest(), authorizedUser);
		}
		catch (EmptyCodeException e) {
			errors.rejectValue("code", "NotNull");
		}
		catch (DuplicateCodeException e) {
			errors.rejectValue("code", "NotDuplicate");
		}
		if (errors.hasErrors()) {
			logger.debug("Errors: {}", errors);
			return "customfield/create";
		}
		redirectAttributes.addFlashAttribute("savedCustomField", customfield);
		redirectAttributes.addAttribute("language", language);
		redirectAttributes.addAttribute("id", customfield.getId());
		redirectAttributes.addAttribute("query", query);
		return "redirect:/_admin/{language}/customfields/index";
	}
}