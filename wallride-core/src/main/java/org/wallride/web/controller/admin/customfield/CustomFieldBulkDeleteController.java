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
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.domain.CustomField;
import org.wallride.service.CustomFieldService;
import org.wallride.support.AuthorizedUser;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Controller
@RequestMapping(value="/{language}/customfields/bulk-delete", method=RequestMethod.POST)
public class CustomFieldBulkDeleteController {

	@Inject
	private CustomFieldService customFieldService;
	@Inject
	private MessageSourceAccessor messageSourceAccessor;
	
	private static Logger logger = LoggerFactory.getLogger(CustomFieldBulkDeleteController.class);

	@ModelAttribute("query")
	public String query(@RequestParam(required = false) String query) {
		return query;
	}

	@RequestMapping
	public String delete(
			@Valid @ModelAttribute("form") CustomFieldBulkDeleteForm form,
			BindingResult errors,
			String query,
			AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes) {
		redirectAttributes.addAttribute("query", query);

		if (!form.isConfirmed()) {
			errors.rejectValue("confirmed", "Confirmed");
		}
		if (errors.hasErrors()) {
			logger.debug("Errors: {}", errors);
			return "redirect:/_admin/{language}/customfields/index";
		}
		
		Collection<CustomField> customFields = null;
		try {
			customFields = customFieldService.bulkDeleteCustomField(form.buildCustomFieldBulkDeleteRequest(), errors);
		}
		catch (ValidationException e) {
			if (errors.hasErrors()) {
				logger.debug("Errors: {}", errors);
				return "redirect:/_admin/{language}/customfields/index";
			}
			throw e;
		}
		
		List<String> errorMessages = null;
		if (errors.hasErrors()) {
			errorMessages = new ArrayList<>();
			for (ObjectError error : errors.getAllErrors()) {
				errorMessages.add(messageSourceAccessor.getMessage(error));
			}
		}
		
		redirectAttributes.addFlashAttribute("deletedCustomFields", customFields);
		redirectAttributes.addFlashAttribute("errorMessages", errorMessages);
		return "redirect:/_admin/{language}/customfields/index";
	}
}
