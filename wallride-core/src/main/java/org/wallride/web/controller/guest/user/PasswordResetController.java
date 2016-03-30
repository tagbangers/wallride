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

import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.domain.BlogLanguage;
import org.wallride.domain.PasswordResetToken;
import org.wallride.exception.EmailNotFoundException;
import org.wallride.model.PasswordResetTokenCreateRequest;
import org.wallride.model.PasswordUpdateRequest;
import org.wallride.service.UserService;

import javax.inject.Inject;
import java.time.LocalDateTime;

@RequestMapping("/password-reset")
public class PasswordResetController {

	public static final String FORM_MODEL_KEY = "form";
	public static final String ERRORS_MODEL_KEY = BindingResult.MODEL_KEY_PREFIX + FORM_MODEL_KEY;

	public static final String INVALID_PASSOWRD_RESET_LINK_ATTR_NAME = "invalidPasswordResetLink";

	@Inject
	private UserService userService;

	@ModelAttribute(FORM_MODEL_KEY)
	public PasswordResetForm setupPasswordResetForm() {
		return new PasswordResetForm();
	}

	@RequestMapping(method = RequestMethod.GET)
	public String entry() {
		return "user/password-reset1-entry";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String token(
			@Validated @ModelAttribute(FORM_MODEL_KEY) PasswordResetForm form,
			BindingResult errors,
			BlogLanguage blogLanguage,
			RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute(FORM_MODEL_KEY, form);
		redirectAttributes.addFlashAttribute(ERRORS_MODEL_KEY, errors);

		if (errors.hasFieldErrors("email")) {
			return "redirect:/password-reset";
		}

		PasswordResetTokenCreateRequest request = form.toPasswordResetTokenCreateRequest(blogLanguage);
		PasswordResetToken passwordResetToken;
		try {
			passwordResetToken = userService.createPasswordResetToken(request);
		} catch (EmailNotFoundException e) {
			errors.rejectValue("email", "UsedEmail");
			return "redirect:/password-reset";
		}

		redirectAttributes.getFlashAttributes().clear();
		redirectAttributes.addFlashAttribute("passwordResetToken", passwordResetToken);
		return "redirect:/password-reset?step.token";
	}

	@RequestMapping(method = RequestMethod.GET, params = "step.token")
	public String token(Model model) {
		PasswordResetToken passwordResetToken = (PasswordResetToken) model.asMap().get("passwordResetToken");
		if (passwordResetToken == null) {
			return "redirect:/password-reset";
		}
		return "user/password-reset2-token";
	}

	@RequestMapping(value = "/{token}", method = RequestMethod.GET)
	public String edit(
			@PathVariable String token,
			RedirectAttributes redirectAttributes) {
		PasswordResetToken passwordResetToken = userService.getPasswordResetToken(token);
		if (passwordResetToken == null) {
			redirectAttributes.addFlashAttribute(INVALID_PASSOWRD_RESET_LINK_ATTR_NAME, true);
			return "redirect:/password-reset";
		}
		LocalDateTime now = LocalDateTime.now();
		if (now.isAfter(passwordResetToken.getExpiredAt())) {
			redirectAttributes.addFlashAttribute(INVALID_PASSOWRD_RESET_LINK_ATTR_NAME, true);
			return "redirect:/password-reset";
		}

		return "user/password-reset3-reset";
	}

	@RequestMapping(value = "/{token}", method = RequestMethod.PUT)
	public String reset(
			@PathVariable String token,
			@Validated @ModelAttribute(FORM_MODEL_KEY) PasswordResetForm form,
			BindingResult errors,
			BlogLanguage blogLanguage,
			RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute(FORM_MODEL_KEY, form);
		redirectAttributes.addFlashAttribute(ERRORS_MODEL_KEY, errors);

		PasswordResetToken passwordResetToken = userService.getPasswordResetToken(token);
		if (passwordResetToken == null) {
			redirectAttributes.addFlashAttribute(INVALID_PASSOWRD_RESET_LINK_ATTR_NAME, true);
			return "redirect:/password-reset";
		}
		LocalDateTime now = LocalDateTime.now();
		if (now.isAfter(passwordResetToken.getExpiredAt())) {
			redirectAttributes.addFlashAttribute(INVALID_PASSOWRD_RESET_LINK_ATTR_NAME, true);
			return "redirect:/password-reset";
		}

		if (!errors.hasFieldErrors("newPassword")) {
			if (!ObjectUtils.nullSafeEquals(form.getNewPassword(), form.getNewPasswordRetype())) {
				errors.rejectValue("newPasswordRetype", "MatchRetype");
			}
		}
		if (errors.hasFieldErrors("newPassword*")) {
			return "redirect:/password-reset/{token}";
		}

		PasswordUpdateRequest request = new PasswordUpdateRequest()
				.withUserId(passwordResetToken.getUser().getId())
				.withPassword(form.getNewPassword())
				.withLanguage(blogLanguage.getLanguage());
		userService.updatePassword(request, passwordResetToken);

		redirectAttributes.getFlashAttributes().clear();
		return "redirect:/login";
	}
}
