package org.wallride.web.controller.guest.account;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.BlogLanguage;
import org.wallride.core.domain.User;
import org.wallride.core.service.DuplicateEmailException;
import org.wallride.core.service.DuplicateLoginIdException;
import org.wallride.core.service.SignupService;

import javax.inject.Inject;
import javax.validation.Valid;

@Controller
@RequestMapping("/account/signup")
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
		return "account/signup";
	}

	@RequestMapping(method=RequestMethod.POST)
	public String signup(
			@Valid @ModelAttribute("form") SignupForm form,
			BindingResult errors,
			BlogLanguage blogLanguage,
			RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute(FORM_MODEL_KEY, form);
		redirectAttributes.addFlashAttribute(ERRORS_MODEL_KEY, errors);
		redirectAttributes.addAttribute("language", blogLanguage.getLanguage());

		if (errors.hasErrors()) {
			return "redirect:/{language}/account/signup?step.edit";
		}

		try {
			signupService.signup(form.toSignupRequest(), User.Role.VIEWER);
		} catch (DuplicateLoginIdException e) {
			errors.rejectValue("loginId", "NotDuplicate");
			return "redirect:/{language}/account/signup?step.edit";
		} catch (DuplicateEmailException e) {
			errors.rejectValue("email", "NotDuplicate");
			return "redirect:/{language}/account/signup?step.edit";
		}

		redirectAttributes.getFlashAttributes().clear();
		return "redirect:/{language}/";
	}
}
