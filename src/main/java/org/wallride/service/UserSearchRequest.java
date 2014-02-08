package org.wallride.service;

import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.wallride.repository.UserFullTextSearchTerm;

import java.io.Serializable;

@SuppressWarnings("serial")
public class UserSearchRequest implements Serializable {
	
	private String keyword;

	public String getKeyword() {
		return keyword;
	}
	
	public boolean isEmpty() {
		if (StringUtils.hasText(getKeyword())) {
			return false;
		}
		return true;
	}

	public UserFullTextSearchTerm toFullTextSearchTerm() {
		UserFullTextSearchTerm term = new UserFullTextSearchTerm();
		BeanUtils.copyProperties(this, term);
		return term;
	}

	public static class Builder  {

		private String keyword;

		public Builder() {
		}

		public Builder keyword(String keyword) {
			this.keyword = keyword;
			return this;
		}

		public UserSearchRequest build() {
			UserSearchRequest request = new UserSearchRequest();
			request.keyword = keyword;
			return request;
		}
	}
}
