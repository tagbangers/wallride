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

package org.wallride.web.controller.guest.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.User;
import org.wallride.core.service.PasswordUpdateRequest;
import org.wallride.core.service.UserService;
import org.wallride.core.support.AuthorizedUser;

import javax.inject.Inject;

@Controller
@RequestMapping("/settings/password")
public class PasswordUpdateController {

	public static final String FORM_MODEL_KEY = "form";
	public static final String ERRORS_MODEL_KEY = BindingResult.MODEL_KEY_PREFIX + FORM_MODEL_KEY;

	@Inject
	private UserService userService;

	@ModelAttribute(FORM_MODEL_KEY)
	public PasswordUpdateForm setupPasswordUpdateForm() {
		return new PasswordUpdateForm();
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model) {
		PasswordUpdateForm form = new PasswordUpdateForm();
		model.addAttribute(FORM_MODEL_KEY, form);
		return edit(model);
	}

	@RequestMapping(method = RequestMethod.GET, params = "step.edit")
	public String edit(Model model) {
		return "/user/password-update";
	}

	@RequestMapping(method = RequestMethod.PUT)
	public String update(
			@Validated @ModelAttribute(FORM_MODEL_KEY) PasswordUpdateForm form,
			BindingResult errors,
			AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute(FORM_MODEL_KEY, form);
		redirectAttributes.addFlashAttribute(ERRORS_MODEL_KEY, errors);

		if (!errors.hasFieldErrors("newPassword")) {
			if (!ObjectUtils.nullSafeEquals(form.getNewPassword(), form.getNewPasswordRetype())) {
				errors.rejectValue("newPasswordRetype", "MatchRetype");
			}
		}

		if (!errors.hasErrors()) {
			User user = userService.readUserById(authorizedUser.getId());
			PasswordEncoder passwordEncoder = new StandardPasswordEncoder();
			if (!passwordEncoder.matches(form.getCurrentPassword(), user.getLoginPassword())) {
				errors.rejectValue("currentPassword", "MatchCurrentPassword");
			}
		}

		if (errors.hasErrors()) {
			return "redirect:/settings/password?step.edit";
		}

		PasswordUpdateRequest request = new PasswordUpdateRequest()
				.withUserId(authorizedUser.getId())
				.withPassword(form.getNewPassword());
		userService.updatePassword(request, authorizedUser);

		redirectAttributes.getFlashAttributes().clear();
		redirectAttributes.addFlashAttribute("updatedPassword", true);
		return "redirect:/settings/password";
	}
}
