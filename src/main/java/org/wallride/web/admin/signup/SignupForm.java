package org.wallride.web.admin.signup;

import org.hibernate.validator.constraints.Email;
import org.wallride.domain.PersonalName;
import org.wallride.service.SignupRequest;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@SuppressWarnings("serial")
public class SignupForm implements Serializable {

	@NotNull
	private String token;

	@NotNull
	private String loginId;
	
	@NotNull
	private String loginPassword;

	@Valid
	private Name name = new Name();

	@NotNull
	@Email
	private String email;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	public String getLoginPassword() {
		return loginPassword;
	}

	public void setLoginPassword(String loginPassword) {
		this.loginPassword = loginPassword;
	}

	public Name getName() {
		return name;
	}

	public void setName(Name name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public SignupRequest buildSignupRequest() {
		SignupRequest.Builder builder = new SignupRequest.Builder();
		return builder
				.token(token)
				.loginId(loginId)
				.loginPassword(loginPassword)
				.name(new PersonalName(name.firstName, name.lastName))
				.email(email)
				.build();
	}

	public static class Name {

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
