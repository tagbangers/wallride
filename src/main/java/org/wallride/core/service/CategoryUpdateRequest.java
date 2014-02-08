package org.wallride.core.service;

import java.io.Serializable;

@SuppressWarnings("serial")
public class CategoryUpdateRequest implements Serializable {

	private Long id;
	private Long parentId;
	private String code;
	private String name;
	private String description;
	private String language;

	public Long getId() {
		return id;
	}

	public Long getParentId() {
		return parentId;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getLanguage() {
		return language;
	}

	public static class Builder  {

		private Long id;
		private Long parentId;
		private String code;
		private String name;
		private String description;
		private String language;

		public Builder() {
		}

		public Builder id(Long id) {
			this.id = id;
			return this;
		}

		public Builder parentId(Long parentId) {
			this.parentId = parentId;
			return this;
		}

		public Builder code(String code) {
			this.code = code;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder description(String description) {
			this.description = description;
			return this;
		}

		public Builder language(String language) {
			this.language = language;
			return this;
		}

		public CategoryUpdateRequest build() {
			CategoryUpdateRequest request = new CategoryUpdateRequest();
			request.id = id;
			request.parentId = parentId;
			request.code = code;
			request.name = name;
			request.description = description;
			request.language = language;
			return request;
		}
	}
}
