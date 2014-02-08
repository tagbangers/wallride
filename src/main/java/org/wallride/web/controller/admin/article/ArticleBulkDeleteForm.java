package org.wallride.web.controller.admin.article;

import org.wallride.core.service.ArticleBulkDeleteRequest;
import org.wallride.web.support.DomainObjectBulkDeleteForm;

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

	public ArticleBulkDeleteRequest buildArticleBulkDeleteRequest() {
		ArticleBulkDeleteRequest.Builder builder = new ArticleBulkDeleteRequest.Builder();
		return builder
				.ids(ids)
				.language(language)
				.build();
	}
}
