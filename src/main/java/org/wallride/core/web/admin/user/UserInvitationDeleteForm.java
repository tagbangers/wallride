package org.wallride.core.web.admin.user;

import org.wallride.core.service.UserInvitationDeleteRequest;
import org.wallride.core.service.UserInvitationResendRequest;
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

	public UserInvitationDeleteRequest buildUserInvitationDeleteRequest() {
		UserInvitationDeleteRequest.Builder builder = new UserInvitationDeleteRequest.Builder();
		return builder
				.token(token)
				.build();
	}
}
