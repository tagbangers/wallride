package org.wallride.core.web.admin.user;

import org.wallride.core.web.DomainObjectDeleteForm;

import javax.validation.constraints.NotNull;

public class UserInvitationDeleteForm extends DomainObjectDeleteForm {

	@NotNull
	private String token;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
