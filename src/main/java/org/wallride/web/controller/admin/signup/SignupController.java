package org.wallride.web.controller.admin.signup;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.wallride.core.domain.UserInvitation;
import org.wallride.core.service.DuplicateEmailException;
import org.wallride.core.service.DuplicateLoginIdException;
import org.wallride.core.service.SignupService;
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
			signupService.signup(form.buildSignupRequest());
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
