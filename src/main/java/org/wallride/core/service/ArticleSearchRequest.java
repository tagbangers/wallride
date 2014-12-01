package org.wallride.core.service;

import org.joda.time.LocalDateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.wallride.core.domain.Post;
import org.wallride.core.repository.ArticleFullTextSearchTerm;

import java.io.Serializable;
import java.util.Collection;

public class ArticleSearchRequest implements Serializable {

	private String keyword;
	private LocalDateTime dateFrom;
	private LocalDateTime dateTo;
	private Collection<Long> categoryIds;
	private Collection<Long> tagIds;
	private Long authorId;
	private Post.Status status;
	private String language;

	public ArticleSearchRequest() {
		this.language = LocaleContextHolder.getLocale().getLanguage();
	}

	public String getKeyword() {
		return keyword;
	}

	public LocalDateTime getDateFrom() {
		return dateFrom;
	}

	public LocalDateTime getDateTo() {
		return dateTo;
	}

	public Collection<Long> getCategoryIds() {
		return categoryIds;
	}

	public Collection<Long> getTagIds() {
		return tagIds;
	}

	public Long getAuthorId() {
		return authorId;
	}

	public Post.Status getStatus() {
		return status;
	}

	public String getLanguage() {
		return language;
	}

	public boolean isEmpty() {
		if (StringUtils.hasText(getKeyword())) {
			return false;
		}
		if (getDateFrom() != null) {
			return false;
		}
		if (getDateTo() != null) {
			return false;
		}
		if (!CollectionUtils.isEmpty(getCategoryIds())) {
			return false;
		}
		if (!CollectionUtils.isEmpty(getTagIds())) {
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

	public ArticleFullTextSearchTerm toFullTextSearchTerm() {
		ArticleFullTextSearchTerm term = new ArticleFullTextSearchTerm();
		BeanUtils.copyProperties(this, term);
		return term;
	}

	public static class Builder  {

		private String keyword;
		private LocalDateTime dateFrom;
		private LocalDateTime dateTo;
		private Collection<Long> categoryIds;
		private Collection<Long> tagIds;
		private Long authorId;
		private Post.Status status;
		private String language;

		public Builder() {
		}

		public Builder keyword(String keyword) {
			this.keyword = keyword;
			return this;
		}

		public Builder dateFrom(LocalDateTime dateFrom) {
			this.dateFrom = dateFrom;
			return this;
		}

		public Builder dateTo(LocalDateTime dateTo) {
			this.dateTo = dateTo;
			return this;
		}

		public Builder categoryIds(Collection<Long> categoryIds) {
			this.categoryIds = categoryIds;
			return this;
		}

		public Builder tagIds(Collection<Long> tagIds) {
			this.tagIds = tagIds;
			return this;
		}

		public Builder authorId(Long authorId) {
			this.authorId = authorId;
			return this;
		}

		public Builder status(Post.Status status) {
			this.status = status;
			return this;
		}

		public Builder language(String language) {
			this.language = language;
			return this;
		}

		public ArticleSearchRequest build() {
			ArticleSearchRequest request = new ArticleSearchRequest();
			request.keyword = keyword;
			request.dateFrom = dateFrom;
			request.dateTo = dateTo;
			request.categoryIds = categoryIds;
			request.tagIds = tagIds;
			request.authorId = authorId;
			request.status = status;
			request.language = language;
			return request;
		}
	}
}
