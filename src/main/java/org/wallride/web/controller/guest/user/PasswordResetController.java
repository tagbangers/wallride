package org.wallride.web.controller.guest.user;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.PasswordResetToken;
import org.wallride.core.service.PasswordResetTokenCreateRequest;
import org.wallride.core.service.UserService;

import javax.inject.Inject;

@Controller
@RequestMapping("/password-reset")
public class PasswordResetController {

	public static final String FORM_MODEL_KEY = "form";
	public static final String ERRORS_MODEL_KEY = BindingResult.MODEL_KEY_PREFIX + FORM_MODEL_KEY;

	@Inject
	private UserService userService;

	@RequestMapping(method = RequestMethod.GET)
	public String entry() {
		return "user/password-reset1-entry";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String token(
			@Validated @ModelAttribute(FORM_MODEL_KEY) PasswordResetForm form,
			BindingResult errors,
			RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute(FORM_MODEL_KEY, form);
		redirectAttributes.addFlashAttribute(ERRORS_MODEL_KEY, errors);

		if (errors.hasErrors()) {
			return "redirect:/settings/email/edit";
		}

		PasswordResetTokenCreateRequest request = new PasswordResetTokenCreateRequest();
		PasswordResetToken passwordResetToken = userService.createPasswordResetToken(request);

		redirectAttributes.getFlashAttributes().clear();
		redirectAttributes.addFlashAttribute("passwordResetToken", passwordResetToken);
		return "redirect:/password-reset?step.token";
	}

	@RequestMapping(method = RequestMethod.GET, params = "step.token")
	public String token() {
		return "user/password-reset2-token";
	}

	@RequestMapping(method = RequestMethod.GET, params = "step.reset")
	public String edit() {
		return "user/password-reset3-reset";
	}

	@RequestMapping(method = RequestMethod.PUT, params = "step.reset")
	public String reset(
			@Validated @ModelAttribute(FORM_MODEL_KEY) PasswordResetForm form,
			BindingResult errors,
			RedirectAttributes redirectAttributes) {
		return "redirect:/login";
	}
}
