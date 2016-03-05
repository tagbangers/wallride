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

package org.wallride.web.controller.admin.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.domain.User;
import org.wallride.service.UserService;
import org.wallride.support.AuthorizedUser;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.ValidationException;

@Controller
@RequestMapping("/{language}/users/edit")
public class UserEditController {
	
	private static Logger logger = LoggerFactory.getLogger(UserEditController.class);
	
	@Inject
	private UserService userService;

	@ModelAttribute("query")
	public String query(@RequestParam(required = false) String query) {
		return query;
	}

	@RequestMapping(method= RequestMethod.GET)
	public String edit(
			@PathVariable String language,
			@RequestParam long id,
			Model model,
			RedirectAttributes redirectAttributes) {
		User user = userService.getUserById(id);
		UserEditForm form = UserEditForm.fromDomainObject(user);
		model.addAttribute("form", form);
		return "user/edit";
	}

	@RequestMapping(method= RequestMethod.POST, params="_step.save")
	public String save(
			@PathVariable String language,
			@Valid @ModelAttribute("form") UserEditForm form,
			BindingResult errors,
			String query,
			AuthorizedUser authorizedUser,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes) {
		if (errors.hasErrors()) {
			return "user/edit";
		}

		User user = null;
		try {
			user = userService.updateUser(form.buildUserUpdateRequest(), errors, authorizedUser);
		}
		catch (ValidationException e) {
			if (errors.hasErrors()) {
				logger.debug("Errors: {}", errors);
				return "user/edit";
			}
			throw e;
		}
		
		redirectAttributes.addFlashAttribute("savedUser", user);
		redirectAttributes.addAttribute("language", language);
		redirectAttributes.addAttribute("id", user.getId());
		redirectAttributes.addAttribute("query", query);
		return "redirect:/_admin/{language}/users/describe";
	}

//	@RequestMapping(method= RequestMethod.POST, params="_step.cancel")
//	public String cancel(
//			@Valid @ModelAttribute("form") UserEditForm form,
//			RedirectAttributes redirectAttributes) {
//		redirectAttributes.addAttribute("id", form.getId());
//		return "redirect:/_admin/users/describe/{id}";
//	}
}