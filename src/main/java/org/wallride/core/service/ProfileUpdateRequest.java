package org.wallride.core.service;

import org.wallride.core.domain.PersonalName;

public class ProfileUpdateRequest {

	private long userId;
	private String email;
	private String loginId;
	private PersonalName name;

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public ProfileUpdateRequest withUserId(long userId) {
		this.userId = userId;
		return this;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public ProfileUpdateRequest withEmail(String email) {
		this.email = email;
		return this;
	}

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	public ProfileUpdateRequest withLoginId(String loginId) {
		this.loginId = loginId;
		return this;
	}

	public PersonalName getName() {
		return name;
	}

	public void setName(PersonalName name) {
		this.name = name;
	}

	public ProfileUpdateRequest withName(String firstName, String lastName) {
		this.name = new PersonalName(firstName, lastName);
		return this;
	}
}
