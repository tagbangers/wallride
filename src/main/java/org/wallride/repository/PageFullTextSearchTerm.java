package org.wallride.repository;

import org.joda.time.LocalDateTime;
import org.wallride.domain.Post;

import java.io.Serializable;

@SuppressWarnings("serial")
public class PageFullTextSearchTerm implements Serializable {

	private Post.Status status = Post.Status.PUBLISHED;

	private String keyword;

	private LocalDateTime dateFrom;

	private LocalDateTime dateTo;

	private String language;

	public Post.Status getStatus() {
		return status;
	}

	public void setStatus(Post.Status status) {
		this.status = status;
	}

	public String getKeyword() {
		return keyword;
	}
	
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public LocalDateTime getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(LocalDateTime dateFrom) {
		this.dateFrom = dateFrom;
	}

	public LocalDateTime getDateTo() {
		return dateTo;
	}

	public void setDateTo(LocalDateTime dateTo) {
		this.dateTo = dateTo;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
}
