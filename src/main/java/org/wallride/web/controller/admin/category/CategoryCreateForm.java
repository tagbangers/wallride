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

package org.wallride.web.controller.admin.category;

import org.springframework.beans.BeanUtils;
import org.wallride.core.domain.Category;
import org.wallride.core.model.CategoryCreateRequest;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@SuppressWarnings("serial")
public class CategoryCreateForm implements Serializable {

	private Long parentId;

	private String code;

	@NotNull
	private String name;

	private String description;

	@NotNull
	private String language;

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public CategoryCreateRequest buildCategoryCreateRequest() {
		CategoryCreateRequest.Builder builder = new CategoryCreateRequest.Builder();
		return builder
				.parentId(parentId)
				.code(code)
				.name(name)
				.description(description)
				.language(language)
				.build();
	}

	public static CategoryCreateForm fromDomainObject(Category category) {
		CategoryCreateForm form = new CategoryCreateForm();
		BeanUtils.copyProperties(category, form);
		return form;
	}
}
