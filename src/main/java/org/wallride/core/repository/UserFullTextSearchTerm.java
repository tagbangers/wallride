package org.wallride.core.repository;

import java.io.Serializable;

@SuppressWarnings("serial")
public class UserFullTextSearchTerm implements Serializable {

	private String keyword;
	
	public String getKeyword() {
		return keyword;
	}
	
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
}
