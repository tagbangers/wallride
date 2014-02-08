package org.wallride.web.admin.user;

import org.springframework.util.StringUtils;
import org.wallride.service.UserSearchRequest;
import org.wallride.web.DomainObjectSearchForm;

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

	public UserSearchRequest buildUserSearchRequest() {
		UserSearchRequest.Builder builder = new UserSearchRequest.Builder();
		return builder
				.keyword(keyword)
				.build();
	}
}
