package org.wallride.core.web.guest.page;

import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.wallride.core.repository.PageFullTextSearchTerm;
import org.wallride.core.web.DomainObjectSearchForm;

@SuppressWarnings("serial")
public class PageSearchForm extends DomainObjectSearchForm {

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

	public PageFullTextSearchTerm toFullTextSearchTerm() {
		PageFullTextSearchTerm term = new PageFullTextSearchTerm();
		BeanUtils.copyProperties(this, term);
		return term;
	}
}
