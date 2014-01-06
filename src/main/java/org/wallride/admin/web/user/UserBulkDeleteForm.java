package org.wallride.admin.web.user;

import org.wallride.core.web.DomainObjectBulkDeleteForm;

import java.util.List;

public class UserBulkDeleteForm extends DomainObjectBulkDeleteForm {

	private List<Long> ids;

	public List<Long> getIds() {
		return ids;
	}

	public void setIds(List<Long> ids) {
		this.ids = ids;
	}
}
