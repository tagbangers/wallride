package org.wallride.service;

import java.io.Serializable;

public class UserDeleteRequest implements Serializable {

	private Long id;

	public Long getId() {
		return id;
	}

	public static class Builder  {

		private Long id;

		public Builder() {
		}

		public Builder id(Long id) {
			this.id = id;
			return this;
		}

		public UserDeleteRequest build() {
			UserDeleteRequest request = new UserDeleteRequest();
			request.id = id;
			return request;
		}
	}
}
