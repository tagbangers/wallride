package org.wallride.core.service;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;

import java.io.Serializable;

@SuppressWarnings("serial")
public class CategorySearchRequest implements Serializable {

	private String keyword;
	private String language;

	public CategorySearchRequest() {
		this.language = LocaleContextHolder.getLocale().getLanguage();
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public CategorySearchRequest withKeyword(String keyword) {
		this.keyword = keyword;
		return this;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public CategorySearchRequest withLanguage(String language) {
		this.language = language;
		return this;
	}

	public boolean isEmpty() {
		if (StringUtils.hasText(getKeyword())) {
			return false;
		}
		if (StringUtils.hasText(getLanguage())) {
			return false;
		}
		return true;
	}
}
