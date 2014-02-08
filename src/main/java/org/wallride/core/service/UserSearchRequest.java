package org.wallride.core.service;

import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.wallride.core.repository.UserFullTextSearchTerm;
import org.wallride.core.web.DomainObjectSearchForm;

@SuppressWarnings("serial")
public class UserSearchRequest extends DomainObjectSearchForm {
	
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
	
	public UserFullTextSearchTerm toFullTextSearchTerm() {
		UserFullTextSearchTerm term = new UserFullTextSearchTerm();
		BeanUtils.copyProperties(this, term);
		return term;
	}
}
