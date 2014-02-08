package org.wallride.service;

import java.io.Serializable;

public class UserInvitationResendRequest implements Serializable {

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

		public UserInvitationResendRequest build() {
			UserInvitationResendRequest request = new UserInvitationResendRequest();
			request.token = token;
			return request;
		}
	}
}
