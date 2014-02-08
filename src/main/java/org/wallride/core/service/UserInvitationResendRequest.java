package org.wallride.core.service;

import org.wallride.core.web.DomainObjectEditForm;

public class UserInvitationResendRequest extends DomainObjectEditForm {

	private String token;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
