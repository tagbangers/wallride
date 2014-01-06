package org.wallride.admin.web.article;

import org.wallride.core.web.DomainObjectBulkDeleteForm;

import javax.validation.constraints.NotNull;
import java.util.List;

public class ArticleBulkDeleteForm extends DomainObjectBulkDeleteForm {

	private List<Long> ids;

	@NotNull
	private String language;

	public List<Long> getIds() {
		return ids;
	}

	public void setIds(List<Long> ids) {
		this.ids = ids;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
}
