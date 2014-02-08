package org.wallride.core.service;

import org.wallride.core.web.DomainObjectCreateForm;

import javax.validation.constraints.NotNull;

public class UserInvitationCreateRequest extends DomainObjectCreateForm {

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
}
