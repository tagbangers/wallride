package org.wallride.core.service;

import org.joda.time.LocalDateTime;
import org.wallride.core.domain.Post;

import java.io.Serializable;

@SuppressWarnings("serial")
public class PageUpdateRequest implements Serializable {
	
	private Long id;
	private String code;
	private String coverId;
	private String title;
	private String body;
	private Long authorId;
	private LocalDateTime date;
	private Long parentId;
//	private Post.Status status;
	private String language;

	public Long getId() {
		return id;
	}

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

	public Long getParentId() {
		return parentId;
	}

//	public Post.Status getStatus() {
//		return status;
//	}

	public String getLanguage() {
		return language;
	}

	public static class Builder  {

		private Long id;
		private String code;
		private String coverId;
		private String title;
		private String body;
		private Long authorId;
		private LocalDateTime date;
		private Long parentId;
		private Post.Status status;
		private String language;

		public Builder() {
		}

		public Builder id(Long id) {
			this.id = id;
			return this;
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

		public Builder parentId(Long parentId) {
			this.parentId = parentId;
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

		public PageUpdateRequest build() {
			PageUpdateRequest request = new PageUpdateRequest();
			request.id = id;
			request.code = code;
			request.coverId = coverId;
			request.title = title;
			request.body = body;
			request.authorId = authorId;
			request.date = date;
			request.parentId = parentId;
//			request.status = status;
			request.language = language;
			return request;
		}
	}
}
