/*
 * Copyright 2014 Tagbangers, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wallride.core.service;

import java.io.Serializable;

@SuppressWarnings("serial")
public class CategoryCreateRequest implements Serializable {

	private Long parentId;
	private String code;
	private String name;
	private String description;
	private String language;

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

		private Long parentId;
		private String code;
		private String name;
		private String description;
		private String language;

		public Builder() {
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

		public CategoryCreateRequest build() {
			CategoryCreateRequest request = new CategoryCreateRequest();
			request.parentId = parentId;
			request.code = code;
			request.name = name;
			request.description = description;
			request.language = language;
			return request;
		}
	}
}
