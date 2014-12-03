package org.wallride.web.controller.admin.category;

import org.wallride.core.service.CategorySearchRequest;
import org.wallride.web.support.DomainObjectSearchForm;

public class CategorySearchForm extends DomainObjectSearchForm {

	private String keyword;
	private String language;

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public CategorySearchRequest toCategorySearchRequest() {
		return new CategorySearchRequest()
				.withKeyword(getKeyword())
				.withLanguage(getLanguage());
	}
}
