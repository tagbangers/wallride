package org.wallride.core.web.admin.user;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.service.UserService;
import org.wallride.core.support.AuthorizedUser;
import org.wallride.core.domain.UserInvitation;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.validation.Valid;

@Controller @Lazy
@RequestMapping("/{language}/users/invitations/resend")
public class UserInvitationResendController {

	@Inject
	private UserService userService;

	@RequestMapping
	public String save(
			@PathVariable String language,
			@Valid @ModelAttribute("form") UserInvitationResendForm form,
			BindingResult result,
			AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes) throws MessagingException {
		UserInvitation invitation = userService.inviteAgain(form, result, authorizedUser);
		redirectAttributes.addFlashAttribute("resentInvitation", invitation);
		return "redirect:/_admin/{language}/users/invitations/index";
	}
}
