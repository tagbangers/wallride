package org.wallride.core.service;

public class PasswordResetTokenCreateRequest {

	private String email;
	private String language;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
}
