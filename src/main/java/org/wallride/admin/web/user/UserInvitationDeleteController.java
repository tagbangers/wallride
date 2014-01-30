package org.wallride.admin.web.user;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.admin.service.UserService;
import org.wallride.admin.support.AuthorizedUser;
import org.wallride.core.domain.UserInvitation;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.validation.Valid;

@Controller @Lazy
@RequestMapping("/{language}/users/invitations/delete")
public class UserInvitationDeleteController {

	@Inject
	private UserService userService;

	@RequestMapping
	public String delele(
			@PathVariable String language,
			@Valid @ModelAttribute("form") UserInvitationDeleteForm form,
			BindingResult result,
			AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes) throws MessagingException {
		UserInvitation invitation = userService.deleteUserInvitation(form);
		redirectAttributes.addFlashAttribute("deletedInvitation", invitation);
		return "redirect:/_admin/{language}/users/invitations/index";
	}
}
