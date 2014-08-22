package org.wallride.core.service;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TagUpdateRequest implements Serializable {

	private Long id;
	private String name;
	private String language;

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getLanguage() {
		return language;
	}

	public static class Builder  {

		private Long id;
		private String name;
		private String language;

		public Builder id(Long id) {
			this.id = id;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder language(String language) {
			this.language = language;
			return this;
		}

		public TagUpdateRequest build() {
			TagUpdateRequest request = new TagUpdateRequest();
			request.id = id;
			request.name = name;
			request.language = language;
			return request;
		}
	}
}
