package org.wallride.core.service;

import org.joda.time.LocalDateTime;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class ArticleCreateRequest implements Serializable {

	private String code;
	private String coverId;
	private String title;
	private String body;
	private Long authorId;
	private LocalDateTime date;
	private Set<Long> categoryIds = new HashSet<>();
	private Set<Long> tagIds = new HashSet<>();
	private String language;

	public String getCode() {
		return code;
	}

	public String getCoverId() {
		return coverId;
	}

	public String getTitle() {
		return title;
	}

	public String getBody() {
		return body;
	}

	public Long getAuthorId() {
		return authorId;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public Set<Long> getCategoryIds() {
		return categoryIds;
	}

	public Set<Long> getTagIds() {
		return tagIds;
	}

	public String getLanguage() {
		return language;
	}

	public static class Builder  {

		private String code;
		private String coverId;
		private String title;
		private String body;
		private Long authorId;
		private LocalDateTime date;
		private Set<Long> categoryIds = new HashSet<>();
		private Set<Long> tagIds = new HashSet<>();
		private String language;

		public Builder() {
		}

		public Builder code(String code) {
			this.code = code;
			return this;
		}

		public Builder coverId(String coverId) {
			this.coverId = coverId;
			return this;
		}

		public Builder title(String title) {
			this.title = title;
			return this;
		}

		public Builder body(String body) {
			this.body = body;
			return this;
		}

		public Builder authorId(Long authorId) {
			this.authorId = authorId;
			return this;
		}

		public Builder date(LocalDateTime date) {
			this.date = date;
			return this;
		}

		public Builder categoryIds(Set<Long> categoryIds) {
			this.categoryIds = categoryIds;
			return this;
		}

		public Builder tagIds(Set<Long> tagIds) {
			this.tagIds = tagIds;
			return this;
		}

		public Builder language(String language) {
			this.language = language;
			return this;
		}

		public ArticleCreateRequest build() {
			ArticleCreateRequest request = new ArticleCreateRequest();
			request.code = code;
			request.coverId = coverId;
			request.title = title;
			request.body = body;
			request.authorId = authorId;
			request.date = date;
			request.categoryIds = categoryIds;
			request.tagIds = tagIds;
			request.language = language;
			return request;
		}
	}
}
