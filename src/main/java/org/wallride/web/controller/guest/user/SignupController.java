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

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.User;
import org.wallride.core.exception.DuplicateEmailException;
import org.wallride.core.exception.DuplicateLoginIdException;
import org.wallride.core.service.SignupService;

import javax.inject.Inject;
import javax.validation.Valid;

@Controller
@RequestMapping("/signup")
public class SignupController {

	public static final String FORM_MODEL_KEY = "form";
	public static final String ERRORS_MODEL_KEY = BindingResult.MODEL_KEY_PREFIX + FORM_MODEL_KEY;

	@Inject
	private SignupService signupService;

	@ModelAttribute(FORM_MODEL_KEY)
	public SignupForm setupSignupForm() {
		return new SignupForm();
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model) {
		SignupForm form = new SignupForm();
		model.addAttribute(FORM_MODEL_KEY, form);
		return edit(model);
	}

	@RequestMapping(method = RequestMethod.GET, params = "step.edit")
	public String edit(Model model) {
		return "user/signup";
	}

	@RequestMapping(method=RequestMethod.POST)
	public String signup(
			@Valid @ModelAttribute("form") SignupForm form,
			BindingResult errors,
			RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute(FORM_MODEL_KEY, form);
		redirectAttributes.addFlashAttribute(ERRORS_MODEL_KEY, errors);

		if (errors.hasErrors()) {
			return "redirect:/signup?step.edit";
		}

		try {
			signupService.signup(form.toSignupRequest(), User.Role.VIEWER);
		} catch (DuplicateLoginIdException e) {
			errors.rejectValue("loginId", "NotDuplicate");
			return "redirect:/signup?step.edit";
		} catch (DuplicateEmailException e) {
			errors.rejectValue("email", "NotDuplicate");
			return "redirect:/signup?step.edit";
		}

		redirectAttributes.getFlashAttributes().clear();
		return "redirect:/";
	}
}
