package org.wallride.web.controller.admin.tag;

import org.wallride.core.service.TagSearchRequest;
import org.wallride.web.support.DomainObjectSearchForm;

public class TagSearchForm extends DomainObjectSearchForm {

	private String keyword;

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public TagSearchRequest buildTagSearchRequest() {
		TagSearchRequest.Builder builder = new TagSearchRequest.Builder();
		return builder
				.keyword(keyword)
				.build();
	}

}
