package org.wallride.core.service;

import org.wallride.core.web.DomainObjectBulkDeleteForm;

import java.io.Serializable;
import java.util.List;

public class UserBulkDeleteRequest implements Serializable {

	private List<Long> ids;

	public List<Long> getIds() {
		return ids;
	}

	public static class Builder  {

		private List<Long> ids;

		public Builder() {
		}

		public Builder ids(List<Long> ids) {
			this.ids = ids;
			return this;
		}

		public UserBulkDeleteRequest build() {
			UserBulkDeleteRequest request = new UserBulkDeleteRequest();
			request.ids = ids;
			return request;
		}
	}
}
