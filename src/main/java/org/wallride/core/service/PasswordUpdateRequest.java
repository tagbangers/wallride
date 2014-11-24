package org.wallride.core.service;

public class PasswordUpdateRequest {

	private long userId;
	private String password;
	private String language;

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

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public PasswordUpdateRequest withLanguage(String language) {
		this.language = language;
		return this;
	}
}
