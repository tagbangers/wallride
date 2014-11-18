package org.wallride.core.service;

public class PasswordUpdateRequest {

	private long userId;
	private String password;

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public PasswordUpdateRequest withUserId(long userId) {
		this.userId = userId;
		return this;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public PasswordUpdateRequest withPassword(String password) {
		this.password = password;
		return this;
	}
}
