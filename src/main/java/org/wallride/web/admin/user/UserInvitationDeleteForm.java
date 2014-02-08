package org.wallride.web.admin.user;

import org.wallride.service.UserInvitationDeleteRequest;
import org.wallride.web.DomainObjectDeleteForm;

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
