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

package org.wallride.web.controller.admin.signup;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.wallride.domain.User;
import org.wallride.domain.UserInvitation;
import org.wallride.exception.DuplicateEmailException;
import org.wallride.exception.DuplicateLoginIdException;
import org.wallride.service.SignupService;
import org.wallride.web.support.HttpForbiddenException;

import javax.inject.Inject;
import javax.validation.Valid;

@Controller
@RequestMapping("/signup")
public class SignupController {

	@Inject
	private SignupService signupService;

	@ModelAttribute("form")
	public SignupForm signupForm(@RequestParam String token) {
		UserInvitation invitation = signupService.readUserInvitation(token);
		boolean valid = signupService.validateInvitation(invitation);
		if (!valid) {
			throw new HttpForbiddenException();
		}
		SignupForm form = new SignupForm();
		form.setToken(token);
		form.setEmail(invitation.getEmail());
		return form;
	}

	@RequestMapping(method=RequestMethod.GET)
	public String signup() {
		return "signup/signup";
	}

	@RequestMapping(method=RequestMethod.POST)
	public String save(
			@Valid @ModelAttribute("form") SignupForm form,
			BindingResult errors) {
		if (errors.hasErrors()) {
			return "signup/signup";
		}

		try {
			signupService.signup(form.toSignupRequest(), User.Role.ADMIN, form.getToken());
		} catch (DuplicateLoginIdException e) {
			errors.rejectValue("loginId", "NotDuplicate");
			return "signup/signup";
		} catch (DuplicateEmailException e) {
			errors.rejectValue("email", "NotDuplicate");
			return "signup/signup";
		}

		return "redirect:/_admin/login";
	}
}
