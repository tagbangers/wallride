package org.wallride.core.service;

import org.wallride.core.domain.PersonalName;

import java.io.Serializable;

@SuppressWarnings("serial")
public class UserUpdateRequest implements Serializable {

	private Long id;
	private String loginId;
	private PersonalName name;
	private String nickname;
	private String email;
	private String description;

	public Long getId() {
		return id;
	}

	public String getLoginId() {
		return loginId;
	}

	public PersonalName getName() {
		return name;
	}

	public String getNickname() {
		return nickname;
	}

	public String getEmail() {
		return email;
	}

	public String getDescription() {
		return description;
	}

	public static class Builder  {

		private Long id;
		private String loginId;
		private PersonalName name;
		private String nickname;
		private String email;
		private String description;

		public Builder() {
		}

		public Builder id(Long id) {
			this.id = id;
			return this;
		}

		public Builder loginId(String loginId) {
			this.loginId = loginId;
			return this;
		}

		public Builder name(PersonalName name) {
			this.name = name;
			return this;
		}

		public Builder nickname(String nickname) {
			this.nickname = nickname;
			return this;
		}

		public Builder email(String email) {
			this.email = email;
			return this;
		}

		public Builder description(String description) {
			this.description = description;
			return this;
		}

		public UserUpdateRequest build() {
			UserUpdateRequest request = new UserUpdateRequest();
			request.id = id;
			request.loginId = loginId;
			request.name = name;
			request.nickname = nickname;
			request.email = email;
			request.description = description;
			return request;
		}
	}
}
