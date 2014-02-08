package org.wallride.web.admin.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.wallride.core.service.UserService;

import javax.inject.Inject;
import javax.validation.Valid;

@Controller
@RequestMapping(value="/{language}/users/delete", method= RequestMethod.POST)
public class UserDeleteController {
	
	private static Logger logger = LoggerFactory.getLogger(UserDeleteController.class);
	
	@Inject
	private UserService userService;
	
	@RequestMapping
	public String delete(@Valid @ModelAttribute("form") UserDeleteForm form, BindingResult errors) {
		if (!form.isConfirmed()) {
			errors.rejectValue("confirmed", "Confirmed");
		}
		if (errors.hasErrors()) {
			logger.debug("Errors: {}", errors);
			return "/user/delete";
		}
		
		try {
			userService.deleteUser(form.buildUserDeleteRequest(), errors);
		}
		catch (BindException e) {
			if (errors.hasErrors()) {
				logger.debug("Errors: {}", errors);
				return "/user/delete";
			}
			throw new RuntimeException(e);
		}
		
		return "/user/delete";
	}
}
