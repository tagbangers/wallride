package org.wallride.web.admin.article;

import org.wallride.core.service.ArticleDeleteRequest;
import org.wallride.web.DomainObjectDeleteForm;

import javax.validation.constraints.NotNull;

public class ArticleDeleteForm extends DomainObjectDeleteForm {

	@NotNull
	private Long id;

	@NotNull
	private String language;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public ArticleDeleteRequest buildArticleDeleteRequest() {
		ArticleDeleteRequest.Builder builder = new ArticleDeleteRequest.Builder();
		return builder
				.id(id)
				.language(language)
				.build();
	}
}
