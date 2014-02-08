package org.wallride.core.web.admin.user;

import org.wallride.core.service.UserBulkDeleteRequest;
import org.wallride.core.service.UserDeleteRequest;
import org.wallride.core.web.DomainObjectDeleteForm;

import javax.validation.constraints.NotNull;

public class UserDeleteForm extends DomainObjectDeleteForm {

	@NotNull
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UserDeleteRequest buildUserDeleteRequest() {
		UserDeleteRequest.Builder builder = new UserDeleteRequest.Builder();
		return builder
				.id(id)
				.build();
	}
}
