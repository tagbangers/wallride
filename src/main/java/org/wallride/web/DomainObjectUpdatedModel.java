package org.wallride.web;

import org.wallride.core.domain.DomainObject;

import java.io.Serializable;

public class DomainObjectUpdatedModel<ID extends Serializable> implements Serializable {

	private ID id;

	public DomainObjectUpdatedModel(DomainObject<ID> object) {
		this.id = object.getId();
	}

	public ID getId() {
		return id;
	}
}
