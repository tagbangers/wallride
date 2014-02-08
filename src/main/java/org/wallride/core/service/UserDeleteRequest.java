package org.wallride.core.service;

import org.wallride.core.web.DomainObjectDeleteForm;

import javax.validation.constraints.NotNull;

public class UserDeleteRequest extends DomainObjectDeleteForm {

	@NotNull
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
