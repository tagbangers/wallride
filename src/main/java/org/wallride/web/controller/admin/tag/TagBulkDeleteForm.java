package org.wallride.web.controller.admin.tag;

import org.wallride.core.service.TagBulkDeleteRequest;
import org.wallride.web.support.DomainObjectBulkDeleteForm;

import javax.validation.constraints.NotNull;
import java.util.List;

public class TagBulkDeleteForm extends DomainObjectBulkDeleteForm {

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

	public TagBulkDeleteRequest buildTagBulkDeleteRequest() {
		TagBulkDeleteRequest.Builder builder = new TagBulkDeleteRequest.Builder();
		return builder
				.ids(ids)
				.language(language)
				.build();
	}
}
