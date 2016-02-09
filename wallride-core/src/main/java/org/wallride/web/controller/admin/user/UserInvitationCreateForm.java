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

import org.wallride.core.model.UserInvitationCreateRequest;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class UserInvitationCreateForm implements Serializable {

	@NotNull
	private String invitees;

	private String message;

	public String getInvitees() {
		return invitees;
	}

	public void setInvitees(String invitees) {
		this.invitees = invitees;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public UserInvitationCreateRequest buildUserInvitationCreateRequest() {
		UserInvitationCreateRequest.Builder builder = new UserInvitationCreateRequest.Builder();
		return builder
				.invitees(invitees)
				.message(message)
				.build();
	}
}
