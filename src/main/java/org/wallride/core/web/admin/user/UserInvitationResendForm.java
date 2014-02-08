package org.wallride.core.web.admin.user;

import org.wallride.core.web.DomainObjectEditForm;

public class UserInvitationResendForm extends DomainObjectEditForm {

	private String token;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
