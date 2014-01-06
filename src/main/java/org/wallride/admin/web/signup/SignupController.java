package org.wallride.admin.web.signup;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.admin.service.SignupService;
import org.wallride.core.domain.UserInvitation;

import javax.inject.Inject;
import javax.validation.Valid;

@Controller
@RequestMapping("/signup")
public class SignupController {

	@Inject
	private SignupService signupService;

	@ModelAttribute("form")
	public SignupForm signupForm() {
		return new SignupForm();
	}

	@RequestMapping(method=RequestMethod.GET)
	public String signup(@RequestParam String token, @ModelAttribute("form") SignupForm form) {
		UserInvitation invitation = signupService.readUserInvitation(token);
		boolean valid = signupService.validateInvitation(invitation);
		if (!valid) {
			return "/signup/invalid";
		}
		form.setEmail(invitation.getEmail());
		return "/signup/signup";
	}

	@RequestMapping(method=RequestMethod.POST)
	public String save(
			@Valid @ModelAttribute("form") SignupForm form,
			BindingResult result,
			RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			return "/signup/signup";
		}
		signupService.signup(form, result);
		return "redirect:/_admin/login";
	}
}
