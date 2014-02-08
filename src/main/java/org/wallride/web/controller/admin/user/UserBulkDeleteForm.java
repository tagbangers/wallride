package org.wallride.web.controller.admin.user;

import org.wallride.core.service.UserBulkDeleteRequest;
import org.wallride.web.support.DomainObjectBulkDeleteForm;

import java.util.List;

public class UserBulkDeleteForm extends DomainObjectBulkDeleteForm {

	private List<Long> ids;

	public List<Long> getIds() {
		return ids;
	}

	public void setIds(List<Long> ids) {
		this.ids = ids;
	}

	public UserBulkDeleteRequest buildUserBulkDeleteRequest() {
		UserBulkDeleteRequest.Builder builder = new UserBulkDeleteRequest.Builder();
		return builder
				.ids(ids)
				.build();
	}
}
