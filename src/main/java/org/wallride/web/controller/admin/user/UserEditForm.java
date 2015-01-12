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

import org.springframework.beans.BeanUtils;
import org.wallride.core.domain.PersonalName;
import org.wallride.core.domain.User;
import org.wallride.core.service.UserUpdateRequest;
import org.wallride.web.support.DomainObjectEditForm;

import javax.validation.constraints.NotNull;

@SuppressWarnings("serial")
public class UserEditForm extends DomainObjectEditForm {

	@NotNull
	private Long id;
	private PersonalName name;
	private String nickname;
	private String email;
	private String description;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public PersonalName getName() {
		return name;
	}

	public void setName(PersonalName name) {
		this.name = name;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public UserUpdateRequest buildUserUpdateRequest() {
		UserUpdateRequest.Builder builder = new UserUpdateRequest.Builder();
		return builder
				.id(id)
				.name(name)
				.nickname(nickname)
				.email(email)
				.description(description)
				.build();
	}

	public static UserEditForm fromDomainObject(User user) {
		UserEditForm form = new UserEditForm();
		BeanUtils.copyProperties(user, form);
		return form;
	}
}
