package org.wallride.core.service;

import java.io.Serializable;

public class UserInvitationCreateRequest implements Serializable {

	private String invitees;
	private String message;

	public String getInvitees() {
		return invitees;
	}

	public String getMessage() {
		return message;
	}

	public static class Builder  {

		private String invitees;
		private String message;

		public Builder() {
		}

		public Builder invitees(String invitees) {
			this.invitees = invitees;
			return this;
		}

		public Builder message(String message) {
			this.message = message;
			return this;
		}

		public UserInvitationCreateRequest build() {
			UserInvitationCreateRequest request = new UserInvitationCreateRequest();
			request.invitees = invitees;
			request.message = message;
			return request;
		}
	}
}
