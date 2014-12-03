package org.wallride.core.service;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;
import org.wallride.core.domain.BlogLanguage;
import org.wallride.core.domain.Post;

import java.io.Serializable;

@SuppressWarnings("serial")
public class PageSearchRequest implements Serializable {

	private String keyword;
	private Long authorId;
	private Post.Status status;
	private String language;

	public PageSearchRequest() {
		this.language = LocaleContextHolder.getLocale().getLanguage();
	}

	public PageSearchRequest(BlogLanguage blogLanguage) {
		this.language = blogLanguage.getLanguage();
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public PageSearchRequest withKeyword(String keyword) {
		this.keyword = keyword;
		return this;
	}

	public Long getAuthorId() {
		return authorId;
	}

	public void setAuthorId(Long authorId) {
		this.authorId = authorId;
	}

	public PageSearchRequest withAuthorId(Long authorId) {
		this.authorId = authorId;
		return this;
	}

	public Post.Status getStatus() {
		return status;
	}

	public void setStatus(Post.Status status) {
		this.status = status;
	}

	public PageSearchRequest withStatus(Post.Status status) {
		this.status = status;
		return this;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public PageSearchRequest withLanguage(String language) {
		this.language = language;
		return this;
	}

	public boolean isEmpty() {
		if (StringUtils.hasText(getKeyword())) {
			return false;
		}
		if (getAuthorId() != null) {
			return false;
		}
		if (getStatus() != null) {
			return false;
		}
		if (StringUtils.hasText(getLanguage())) {
			return false;
		}
		return true;
	}
}
