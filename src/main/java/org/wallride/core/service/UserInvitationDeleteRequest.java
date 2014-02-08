package org.wallride.core.service;

import org.wallride.core.web.DomainObjectDeleteForm;

import javax.validation.constraints.NotNull;

public class UserInvitationDeleteRequest extends DomainObjectDeleteForm {

	@NotNull
	private String token;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
