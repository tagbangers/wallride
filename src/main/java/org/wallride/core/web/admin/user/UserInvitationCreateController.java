package org.wallride.core.web.admin.user;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.service.UserService;
import org.wallride.core.support.AuthorizedUser;
import org.wallride.core.domain.UserInvitation;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.validation.Valid;
import java.util.List;

@Controller @Lazy
@RequestMapping("/{language}/users/invitations/create")
public class UserInvitationCreateController {

	@Inject
	private UserService userService;

	@RequestMapping(method= RequestMethod.POST)
	public String save(
			@PathVariable String language,
			@Valid @ModelAttribute("form") UserInvitationCreateForm form,
			BindingResult result,
			AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes) throws MessagingException {
		if (result.hasErrors()) {
			return "/user/invitation/index";
		}
		List<UserInvitation> invitations = userService.inviteUsers(form, result, authorizedUser);
		redirectAttributes.addFlashAttribute("savedInvitations", invitations);
		return "redirect:/_admin/{language}/users/invitations/index";
	}
}
