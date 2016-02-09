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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.wallride.core.domain.UserInvitation;
import org.wallride.core.service.UserService;

import javax.inject.Inject;
import java.util.List;

@Controller
@RequestMapping("/{language}/users/invitations/index")
public class UserInvitationIndexController {

	@Inject
	private UserService userService;

	@ModelAttribute("invitations")
	public List<UserInvitation> userInvitations() {
		return userService.getUserInvitations();
	}

	@ModelAttribute("form")
	public UserInvitationCreateForm userInviteForm() {
		return new UserInvitationCreateForm();
	}

	@ModelAttribute("query")
	public String query(@RequestParam(required = false) String query) {
		return query;
	}

	@RequestMapping(method= RequestMethod.GET)
	public String index() {
		return "user/invitation/index";
	}
}
