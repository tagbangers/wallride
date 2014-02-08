package org.wallride.web.admin.user;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.wallride.service.UserService;
import org.wallride.domain.UserInvitation;

import javax.inject.Inject;
import java.util.List;

@Controller @Lazy
@RequestMapping("/{language}/users/invitations/index")
public class UserInvitationIndexController {

	@Inject
	private UserService userService;

	@ModelAttribute("invitations")
	public List<UserInvitation> userInvitations() {
		return userService.readUserInvitations();
	}

	@ModelAttribute("form")
	public UserInvitationCreateForm userInviteForm() {
		return new UserInvitationCreateForm();
	}

	@RequestMapping(method= RequestMethod.GET)
	public String index() {
		return "/user/invitation/index";
	}
}
