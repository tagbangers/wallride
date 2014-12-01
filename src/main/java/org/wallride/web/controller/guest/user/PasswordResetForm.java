package org.wallride.web.controller.guest.user;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.wallride.core.domain.BlogLanguage;
import org.wallride.core.service.PasswordResetTokenCreateRequest;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

public class PasswordResetForm implements Serializable {

	@NotNull
	@Email
	private String email;
	@NotNull
	private String token;
	@NotNull
	@Pattern(regexp = "^\\p{ASCII}*$")
	@Length(min = 8, max = 32)
	private String newPassword;
	@NotNull
	private String newPasswordRetype;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getNewPasswordRetype() {
		return newPasswordRetype;
	}

	public void setNewPasswordRetype(String newPasswordRetype) {
		this.newPasswordRetype = newPasswordRetype;
	}

	public PasswordResetTokenCreateRequest toPasswordResetTokenCreateRequest(BlogLanguage blogLanguage) {
		PasswordResetTokenCreateRequest request = new PasswordResetTokenCreateRequest();
		request.setEmail(getEmail());
		request.setLanguage(blogLanguage.getLanguage());
		return request;
	}
}
