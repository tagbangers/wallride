package org.wallride.core.service;

import java.io.Serializable;

public class UserInvitationDeleteRequest implements Serializable {

	private String token;

	public String getToken() {
		return token;
	}

	public static class Builder  {

		private String token;

		public Builder() {
		}

		public Builder token(String token) {
			this.token = token;
			return this;
		}

		public UserInvitationDeleteRequest build() {
			UserInvitationDeleteRequest request = new UserInvitationDeleteRequest();
			request.token = token;
			return request;
		}
	}
}
