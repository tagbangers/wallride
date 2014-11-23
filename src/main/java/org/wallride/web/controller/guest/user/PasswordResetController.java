package org.wallride.web.controller.guest.user;

import org.joda.time.LocalDateTime;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.BlogLanguage;
import org.wallride.core.domain.PasswordResetToken;
import org.wallride.core.service.EmailNotFoundException;
import org.wallride.core.service.PasswordResetTokenCreateRequest;
import org.wallride.core.service.PasswordUpdateRequest;
import org.wallride.core.service.UserService;

import javax.inject.Inject;

@Controller
@RequestMapping("/password-reset")
public class PasswordResetController {

	public static final String FORM_MODEL_KEY = "form";
	public static final String ERRORS_MODEL_KEY = BindingResult.MODEL_KEY_PREFIX + FORM_MODEL_KEY;

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
			@PathVariable String token) {
		PasswordResetToken passwordResetToken = userService.readPasswordResetToken(token);
		if (passwordResetToken == null) {
			return "redirect:/password-reset";
		}
		LocalDateTime now = new LocalDateTime();
		if (now.isAfter(passwordResetToken.getExpiredAt())) {
			return "redirect:/password-reset";
		}

		return "user/password-reset3-reset";
	}

	@RequestMapping(value = "/{token}", method = RequestMethod.PUT)
	public String reset(
			@PathVariable String token,
			@Validated @ModelAttribute(FORM_MODEL_KEY) PasswordResetForm form,
			BindingResult errors,
			RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute(FORM_MODEL_KEY, form);
		redirectAttributes.addFlashAttribute(ERRORS_MODEL_KEY, errors);

		PasswordResetToken passwordResetToken = userService.readPasswordResetToken(token);
		if (passwordResetToken == null) {
			return "redirect:/password-reset";
		}
		LocalDateTime now = new LocalDateTime();
		if (now.isAfter(passwordResetToken.getExpiredAt())) {
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
				.withPassword(form.getNewPassword());
		userService.updatePassword(request, passwordResetToken);

		redirectAttributes.getFlashAttributes().clear();
		return "redirect:/login";
	}
}
