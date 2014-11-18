package org.wallride.core.service;

import org.joda.time.LocalDateTime;
import org.wallride.core.domain.Post;

import java.util.ArrayList;
import java.util.Collection;

public class PostSearchRequest {

	private Collection<Long> postIds = new ArrayList<>();
	private Post.Status status = Post.Status.PUBLISHED;
	private String keyword;
	private LocalDateTime dateFrom;
	private LocalDateTime dateTo;
	private String language;

	public PostSearchRequest(String language) {
		this.language = language;
	}

	public Collection<Long> getPostIds() {
		return postIds;
	}

	public void setPostIds(Collection<Long> postIds) {
		this.postIds = postIds;
	}

	public PostSearchRequest withPostIds(Long... postIds) {
		if (getPostIds() == null) {
			setPostIds(new ArrayList<Long>(postIds.length));
		}
		for (Long value : postIds) {
			getPostIds().add(value);
		}
		return this;
	}

	public Post.Status getStatus() {
		return status;
	}

	public void setStatus(Post.Status status) {
		this.status = status;
	}
	
	public PostSearchRequest withStatus(Post.Status status) {
		this.status = status;
		return this;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public PostSearchRequest withKeyword(String keyword) {
		this.keyword = keyword;
		return this;
	}

	public LocalDateTime getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(LocalDateTime dateFrom) {
		this.dateFrom = dateFrom;
	}

	public PostSearchRequest withDateFrom(LocalDateTime dateFrom) {
		this.dateFrom = dateFrom;
		return this;
	}

	public LocalDateTime getDateTo() {
		return dateTo;
	}

	public void setDateTo(LocalDateTime dateTo) {
		this.dateTo = dateTo;
	}

	public PostSearchRequest withDateTo(LocalDateTime dateTo) {
		this.dateTo = dateTo;
		return this;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public PostSearchRequest withLanguage(String language) {
		this.language = language;
		return this;
	}
}
