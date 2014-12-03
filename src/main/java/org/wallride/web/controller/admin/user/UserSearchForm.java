package org.wallride.web.controller.admin.user;

import org.springframework.util.StringUtils;
import org.wallride.core.service.UserSearchRequest;
import org.wallride.web.support.DomainObjectSearchForm;

@SuppressWarnings("serial")
public class UserSearchForm extends DomainObjectSearchForm {
	
	private String keyword;

	public String getKeyword() {
		return keyword;
	}
	
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public boolean isEmpty() {
		if (StringUtils.hasText(getKeyword())) {
			return false;
		}
		return true;
	}
	
	public boolean isAdvanced() {
		return false;
	}

	public UserSearchRequest toUserSearchRequest() {
		return new UserSearchRequest()
				.withKeyword(getKeyword());
	}
}
