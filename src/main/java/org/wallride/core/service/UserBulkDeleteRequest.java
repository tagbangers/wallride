package org.wallride.core.service;

import org.wallride.core.web.DomainObjectBulkDeleteForm;

import java.util.List;

public class UserBulkDeleteRequest extends DomainObjectBulkDeleteForm {

	private List<Long> ids;

	public List<Long> getIds() {
		return ids;
	}

	public void setIds(List<Long> ids) {
		this.ids = ids;
	}
}
