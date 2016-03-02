/*
 * Copyright 2014 Tagbangers, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wallride.web.controller.admin.user;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.domain.UserInvitation;
import org.wallride.service.UserService;
import org.wallride.support.AuthorizedUser;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/{language}/users/invitations/create")
public class UserInvitationCreateController {

	@Inject
	private UserService userService;

	@ModelAttribute("query")
	public String query(@RequestParam(required = false) String query) {
		return query;
	}

	@RequestMapping(method= RequestMethod.POST)
	public String save(
			@PathVariable String language,
			@Valid @ModelAttribute("form") UserInvitationCreateForm form,
			BindingResult result,
			String query,
			AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes) throws MessagingException {
		if (result.hasErrors()) {
			return "user/invitation/index";
		}
		List<UserInvitation> invitations = userService.inviteUsers(form.buildUserInvitationCreateRequest(), result, authorizedUser);
		redirectAttributes.addFlashAttribute("savedInvitations", invitations);
		redirectAttributes.addAttribute("query", query);
		return "redirect:/_admin/{language}/users/invitations/index";
	}
}
