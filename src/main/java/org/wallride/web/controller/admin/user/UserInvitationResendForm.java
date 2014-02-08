package org.wallride.web.controller.admin.user;

import org.wallride.core.service.UserInvitationResendRequest;
import org.wallride.web.support.DomainObjectEditForm;

public class UserInvitationResendForm extends DomainObjectEditForm {

	private String token;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public UserInvitationResendRequest buildUserInvitationResendRequest() {
		UserInvitationResendRequest.Builder builder = new UserInvitationResendRequest.Builder();
		return builder
				.token(token)
				.build();
	}
}
