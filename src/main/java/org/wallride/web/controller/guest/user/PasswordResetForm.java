package org.wallride.web.controller.guest.user;

import org.hibernate.validator.constraints.Length;
import org.wallride.core.service.PasswordResetTokenCreateRequest;
import org.wallride.core.support.AuthorizedUser;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

public class PasswordResetForm implements Serializable {

	@NotNull
	private String email;
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

	public PasswordResetTokenCreateRequest toPasswordResetTokenCreateRequest() {
		PasswordResetTokenCreateRequest request = new PasswordResetTokenCreateRequest();
		request.setEmail(getEmail());
		return request;
	}
}
