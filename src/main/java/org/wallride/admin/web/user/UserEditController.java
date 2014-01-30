package org.wallride.admin.web.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.admin.service.UserService;
import org.wallride.admin.support.AuthorizedUser;
import org.wallride.core.domain.User;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.ValidationException;

@Controller @Lazy
@RequestMapping("/{language}/users/edit")
public class UserEditController {
	
	private static Logger logger = LoggerFactory.getLogger(UserEditController.class);
	
	@Inject
	private UserService userService;

	@RequestMapping(method= RequestMethod.GET)
	public String edit(
			@PathVariable String language,
			@RequestParam long id,
			Model model,
			RedirectAttributes redirectAttributes) {
		User user = userService.readUserById(id);
		UserEditForm form = UserEditForm.fromDomainObject(user);
		model.addAttribute("form", form);
		return "/user/edit";
	}

	@RequestMapping(method= RequestMethod.POST, params="_step.save")
	public String save(
			@PathVariable String language,
			@Valid @ModelAttribute("form") UserEditForm form,
			BindingResult errors,
			AuthorizedUser authorizedUser,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes) {
		if (errors.hasErrors()) {
			return "/user/edit";
		}

		User user = null;
		try {
			user = userService.updateUser(form, errors, authorizedUser);
		}
		catch (ValidationException e) {
			if (errors.hasErrors()) {
				logger.debug("Errors: {}", errors);
				return "/user/edit";
			}
			throw e;
		}
		
		redirectAttributes.addFlashAttribute("savedUser", user);
		redirectAttributes.addAttribute("language", language);
		redirectAttributes.addAttribute("id", user.getId());
		return "redirect:/_admin/{language}/user/edit?id={id}";
	}

	@RequestMapping(method= RequestMethod.POST, params="_step.cancel")
	public String cancel(
			@Valid @ModelAttribute("form") UserEditForm form,
			RedirectAttributes redirectAttributes) {
		redirectAttributes.addAttribute("id", form.getId());
		return "redirect:/_admin/user/describe/{id}";
	}
}