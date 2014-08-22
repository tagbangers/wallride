package org.wallride.web.controller.admin.tag;

import org.wallride.core.service.TagSearchRequest;
import org.wallride.web.support.DomainObjectSearchForm;

public class TagSearchForm extends DomainObjectSearchForm {

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

	public TagSearchRequest buildTagSearchRequest() {
		TagSearchRequest.Builder builder = new TagSearchRequest.Builder();
		return builder
				.keyword(keyword)
				.language(language)
				.build();
	}

}
