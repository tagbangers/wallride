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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.domain.UserInvitation;
import org.wallride.service.UserService;
import org.wallride.support.AuthorizedUser;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.validation.Valid;

@Controller
@RequestMapping("/{language}/users/invitations/delete")
public class UserInvitationDeleteController {

	@Inject
	private UserService userService;

	@ModelAttribute("query")
	public String query(@RequestParam(required = false) String query) {
		return query;
	}

	@RequestMapping
	public String delele(
			@PathVariable String language,
			@Valid @ModelAttribute("form") UserInvitationDeleteForm form,
			BindingResult result,
			String query,
			AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes) throws MessagingException {
		UserInvitation invitation = userService.deleteUserInvitation(form.buildUserInvitationDeleteRequest());
		redirectAttributes.addFlashAttribute("deletedInvitation", invitation);
		redirectAttributes.addAttribute("query", query);
		return "redirect:/_admin/{language}/users/invitations/index";
	}
}
