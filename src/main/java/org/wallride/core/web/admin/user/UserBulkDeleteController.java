package org.wallride.core.web.admin.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.service.UserService;
import org.wallride.core.support.AuthorizedUser;
import org.wallride.core.domain.User;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Controller @Lazy
@RequestMapping(value="/{language}/users/bulk-delete", method= RequestMethod.POST)
public class UserBulkDeleteController {

	@Inject
	private UserService userService;
	
	@Inject
	private MessageSourceAccessor messageSourceAccessor;
	
	private static Logger logger = LoggerFactory.getLogger(UserBulkDeleteController.class);

	@RequestMapping
	public String delete(
			@Valid @ModelAttribute("form") UserBulkDeleteForm form,
			BindingResult errors,
			@RequestParam(required=false) String token,
			AuthorizedUser authorizedUser,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes,
			Model model) {
		if (!form.isConfirmed()) {
			errors.rejectValue("confirmed", "Confirmed");
		}
		if (errors.hasErrors()) {
			logger.debug("Errors: {}", errors);
			return "redirect:/_admin/user/";
		}
		
		Collection<User> users = null;
		try {
			users = userService.bulkDeleteUser(form, errors);
		}
		catch (ValidationException e) {
			if (errors.hasErrors()) {
				logger.debug("Errors: {}", errors);
				return "redirect:/_admin/user/";
			}
			throw e;
		}
		
		List<String> errorMessages = null;
		if (errors.hasErrors()) {
			errorMessages = new ArrayList<>();
			for (ObjectError error : errors.getAllErrors()) {
				errorMessages.add(messageSourceAccessor.getMessage(error));
			}
		}
		
		redirectAttributes.addAttribute("token", token);
		redirectAttributes.addFlashAttribute("deletedArticles", users);
		redirectAttributes.addFlashAttribute("errorMessages", errorMessages);
		return "redirect:/_admin/user/?token={token}";
	}
}
