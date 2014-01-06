package org.wallride.admin.web.page;

import org.wallride.core.web.DomainObjectDeleteForm;

import javax.validation.constraints.NotNull;

public class PageDeleteForm extends DomainObjectDeleteForm {

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
}
