package org.wallride.core.service;

import org.joda.time.LocalDateTime;
import org.wallride.core.domain.Post;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class SearchPostRequest {

	private Collection<Long> postIds = new ArrayList<>();
	private Post.Status status = Post.Status.PUBLISHED;
	private String keyword;
	private LocalDateTime dateFrom;
	private LocalDateTime dateTo;
	private Collection<Long> categoryIds = new ArrayList<>();
	private Collection<Long> tagIds = new ArrayList<>();
	private String language;

	public SearchPostRequest(String language) {
		this.language = language;
	}

	public Collection<Long> getPostIds() {
		return postIds;
	}

	public void setPostIds(Collection<Long> postIds) {
		this.postIds = postIds;
	}

	public SearchPostRequest withPostIds(Long... postIds) {
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
	
	public SearchPostRequest withStatus(Post.Status status) {
		this.status = status;
		return this;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public SearchPostRequest withKeyword(String keyword) {
		this.keyword = keyword;
		return this;
	}

	public LocalDateTime getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(LocalDateTime dateFrom) {
		this.dateFrom = dateFrom;
	}

	public SearchPostRequest withDateFrom(LocalDateTime dateFrom) {
		this.dateFrom = dateFrom;
		return this;
	}

	public LocalDateTime getDateTo() {
		return dateTo;
	}

	public void setDateTo(LocalDateTime dateTo) {
		this.dateTo = dateTo;
	}

	public SearchPostRequest withDateTo(LocalDateTime dateTo) {
		this.dateTo = dateTo;
		return this;
	}

	public Collection<Long> getCategoryIds() {
		return categoryIds;
	}

	public void setCategoryIds(Collection<Long> categoryIds) {
		this.categoryIds = categoryIds;
	}

	public SearchPostRequest withCategoryIds(Long... categoryIds) {
		if (getCategoryIds() == null) {
			setCategoryIds(new HashSet<Long>(categoryIds.length));
		}
		for (Long value : categoryIds) {
			getCategoryIds().add(value);
		}
		return this;
	}

	public SearchPostRequest withCategoryIds(Collection<Long> categoryIds) {
		if (categoryIds == null) {
			this.categoryIds = null;
		} else {
			this.categoryIds.addAll(categoryIds);
		}
		return this;
	}

	public Collection<Long> getTagIds() {
		return tagIds;
	}

	public void setTagIds(Collection<Long> tagIds) {
		this.tagIds = tagIds;
	}

	public SearchPostRequest withTagIds(Long... tagIds) {
		if (getTagIds() == null) {
			setTagIds(new HashSet<Long>(tagIds.length));
		}
		for (Long value : tagIds) {
			getTagIds().add(value);
		}
		return this;
	}

	public SearchPostRequest withTagIds(Collection<Long> tagIds) {
		if (tagIds == null) {
			this.tagIds = null;
		} else {
			this.tagIds.addAll(tagIds);
		}
		return this;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public SearchPostRequest withLanguage(String language) {
		this.language = language;
		return this;
	}
}
