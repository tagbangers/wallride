package org.wallride.core.web.admin.user;

import org.wallride.core.service.ArticleSearchRequest;
import org.wallride.core.service.UserInvitationCreateRequest;
import org.wallride.core.web.DomainObjectCreateForm;

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
