package org.wallride.core.service;

import org.wallride.core.domain.PersonalName;

public class ProfileUpdateRequest {

	private long userId;
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
