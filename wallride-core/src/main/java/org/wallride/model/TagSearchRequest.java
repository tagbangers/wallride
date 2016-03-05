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

package org.wallride.model;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TagSearchRequest implements Serializable {

	private String keyword;
	private String language;

	public TagSearchRequest() {
		this.language = LocaleContextHolder.getLocale().getLanguage();
	}

	public String getKeyword() {
		return keyword;
	}

	public String getLanguage() {
		return language;
	}

	public boolean isEmpty() {
		if (StringUtils.hasText(getKeyword())) {
			return false;
		}
		if (StringUtils.hasText(getLanguage())) {
			return false;
		}
		return true;
	}
	
	public static class Builder  {

		private String keyword;
		private String language;

		public Builder() {
		}

		public Builder keyword(String keyword) {
			this.keyword = keyword;
			return this;
		}

		public Builder language(String language) {
			this.language = language;
			return this;
		}

		public TagSearchRequest build() {
			TagSearchRequest request = new TagSearchRequest();
			request.keyword = keyword;
			request.language = language;
			return request;
		}
	}
}
