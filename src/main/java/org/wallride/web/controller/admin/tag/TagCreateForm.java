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

import org.springframework.beans.BeanUtils;
import org.wallride.core.domain.Category;
import org.wallride.core.service.CategoryCreateRequest;
import org.wallride.core.service.TagCreateRequest;
import org.wallride.web.support.DomainObjectCreateForm;

import javax.validation.constraints.NotNull;

@SuppressWarnings("serial")
public class TagCreateForm extends DomainObjectCreateForm {

	@NotNull
	private String name;
	@NotNull
	private String language;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public TagCreateRequest buildTagCreateRequest() {
		TagCreateRequest.Builder builder = new TagCreateRequest.Builder();
		return builder
				.name(name)
				.language(language)
				.build();
	}

	public static TagCreateForm fromDomainObject(Category category) {
		TagCreateForm form = new TagCreateForm();
		BeanUtils.copyProperties(category, form);
		return form;
	}
}
