package org.wallride.core.service;

import org.wallride.core.domain.PersonalName;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SignupRequest implements Serializable {

	private String token;
	private String loginId;
	private String loginPassword;
	private PersonalName name;
	private String email;

	public String getToken() {
		return token;
	}

	public String getLoginId() {
		return loginId;
	}

	public String getLoginPassword() {
		return loginPassword;
	}

	public PersonalName getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public static class Builder  {

		private String token;
		private String loginId;
		private String loginPassword;
		private PersonalName name;
		private String email;

		public Builder() {
		}

		public Builder token(String token) {
			this.token = token;
			return this;
		}

		public Builder loginId(String loginId) {
			this.loginId = loginId;
			return this;
		}

		public Builder loginPassword(String loginPassword) {
			this.loginPassword = loginPassword;
			return this;
		}

		public Builder name(PersonalName name) {
			this.name = name;
			return this;
		}

		public Builder email(String email) {
			this.email = email;
			return this;
		}

		public SignupRequest build() {
			SignupRequest request = new SignupRequest();
			request.token = token;
			request.loginId = loginId;
			request.loginPassword = loginPassword;
			request.name = name;
			request.email = email;
			return request;
		}
	}
}
