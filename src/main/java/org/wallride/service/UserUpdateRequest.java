package org.wallride.service;

import org.joda.time.LocalDateTime;

import java.io.Serializable;

@SuppressWarnings("serial")
public class UserUpdateRequest implements Serializable {

	private Long id;
	private LocalDateTime date;
	private String title;
	private Long coverId;
	private String body;
	private String language;

	public Long getId() {
		return id;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public String getTitle() {
		return title;
	}

	public Long getCoverId() {
		return coverId;
	}

	public String getBody() {
		return body;
	}

	public String getLanguage() {
		return language;
	}

	public static class Builder  {

		private Long id;
		private LocalDateTime date;
		private String title;
		private Long coverId;
		private String body;
		private String language;

		public Builder() {
		}

		public Builder id(Long id) {
			this.id = id;
			return this;
		}

		public Builder date(LocalDateTime date) {
			this.date = date;
			return this;
		}

		public Builder title(String title) {
			this.title = title;
			return this;
		}

		public Builder coverId(Long coverId) {
			this.coverId = coverId;
			return this;
		}

		public Builder body(String body) {
			this.body = body;
			return this;
		}

		public Builder language(String language) {
			this.language = language;
			return this;
		}

		public UserUpdateRequest build() {
			UserUpdateRequest request = new UserUpdateRequest();
			request.id = id;
			request.date = date;
			request.title = title;
			request.coverId = coverId;
			request.body = body;
			request.language = language;
			return request;
		}
	}
}
