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

package org.wallride.web.controller.guest.user;

import org.hibernate.validator.constraints.Email;
import org.wallride.domain.PersonalName;
import org.wallride.model.ProfileUpdateRequest;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

public class ProfileUpdateForm implements Serializable {

	@Email
	private String email;
	@NotNull
	@Pattern(regexp = "^[\\w\\-]+$")
	private String loginId;
	@Valid
	private Name name = new Name();
	@NotNull

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	public Name getName() {
		return name;
	}

	public void setName(Name name) {
		this.name = name;
	}

	public ProfileUpdateRequest toProfileUpdateRequest() {
		ProfileUpdateRequest request = new ProfileUpdateRequest();
		request.setEmail(getEmail());
		request.setLoginId(getLoginId());
		request.setName(new PersonalName(getName().getFirstName(), getName().getLastName()));
		return request;
	}

	public static class Name implements Serializable {

		@NotNull
		private String firstName;
		@NotNull
		private String lastName;

		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		public String getLastName() {
			return lastName;
		}

		public void setLastName(String lastName) {
			this.lastName = lastName;
		}
	}
}
