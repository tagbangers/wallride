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
			return "user/delete";
		}
		
		try {
			userService.deleteUser(form.buildUserDeleteRequest(), errors);
		}
		catch (BindException e) {
			if (errors.hasErrors()) {
				logger.debug("Errors: {}", errors);
				return "user/delete";
			}
			throw new RuntimeException(e);
		}
		
		return "user/delete";
	}
}
