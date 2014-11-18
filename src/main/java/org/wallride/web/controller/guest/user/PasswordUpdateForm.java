package org.wallride.web.controller.guest.user;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

public class PasswordUpdateForm implements Serializable {

	@NotNull
	private String currentPassword;
	@NotNull
	@Pattern(regexp = "^\\p{ASCII}*$")
	@Length(min = 8, max = 32)
	private String newPassword;
	@NotNull
	private String newPasswordRetype;

	public String getCurrentPassword() {
		return currentPassword;
	}

	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
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
}
