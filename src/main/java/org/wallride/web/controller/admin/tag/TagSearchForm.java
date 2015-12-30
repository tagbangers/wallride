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

package org.wallride.web.controller.admin.tag;

import org.springframework.context.i18n.LocaleContextHolder;
import org.wallride.core.service.TagSearchRequest;

import java.io.Serializable;

public class TagSearchForm implements Serializable {

	private String keyword;

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public TagSearchRequest toTagSearchRequest() {
		TagSearchRequest.Builder builder = new TagSearchRequest.Builder();
		return builder
				.keyword(keyword)
				.language(LocaleContextHolder.getLocale().getLanguage())
				.build();
	}
}
