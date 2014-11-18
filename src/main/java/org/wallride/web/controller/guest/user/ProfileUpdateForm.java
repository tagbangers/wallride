package org.wallride.web.controller.guest.user;

import org.hibernate.validator.constraints.Email;
import org.wallride.core.domain.PersonalName;
import org.wallride.core.service.ProfileUpdateRequest;
import org.wallride.core.service.SignupRequest;
import org.wallride.web.support.MaxSize;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

public class ProfileUpdateForm implements Serializable {

	@NotNull
	@Pattern(regexp = "^[\\w\\-]+$")
	private String loginId;
	@Valid
	private Name name = new Name();
	@NotNull
	@Email
	private String email;

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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public ProfileUpdateRequest toProfileUpdateRequest() {
		ProfileUpdateRequest request = new ProfileUpdateRequest();
//		request.setEmail(getEmail());
//		request.setLoginId(getLoginId());
//		request.setLoginPassword(getLoginPassword());
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
