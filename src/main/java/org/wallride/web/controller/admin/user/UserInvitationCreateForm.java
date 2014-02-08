package org.wallride.web.controller.admin.user;

import org.wallride.core.service.UserInvitationCreateRequest;
import org.wallride.web.support.DomainObjectCreateForm;

import javax.validation.constraints.NotNull;

public class UserInvitationCreateForm extends DomainObjectCreateForm {

	@NotNull
	private String invitees;

	private String message;

	public String getInvitees() {
		return invitees;
	}

	public void setInvitees(String invitees) {
		this.invitees = invitees;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public UserInvitationCreateRequest buildUserInvitationCreateRequest() {
		UserInvitationCreateRequest.Builder builder = new UserInvitationCreateRequest.Builder();
		return builder
				.invitees(invitees)
				.message(message)
				.build();
	}
}
