package org.wallride.core.service;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TagCreateRequest implements Serializable {

	private String name;
	private String language;

	public String getName() {
		return name;
	}

	public String getLanguage() {
		return language;
	}

	public static class Builder  {

		private String name;
		private String language;

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder language(String language) {
			this.language = language;
			return this;
		}

		public TagCreateRequest build() {
			TagCreateRequest request = new TagCreateRequest();
			request.name = name;
			request.language = language;
			return request;
		}
	}
}
