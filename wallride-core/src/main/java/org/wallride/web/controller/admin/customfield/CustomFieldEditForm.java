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

package org.wallride.web.controller.admin.customfield;

import org.springframework.beans.BeanUtils;
import org.wallride.core.domain.CustomField;
import org.wallride.core.model.CustomFieldUpdateRequest;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("serial")
public class CustomFieldEditForm implements Serializable {

	@NotNull
	private Long id;

	@NotNull
	private String name;

	@NotNull
	private CustomField.FieldType type;

	private String description;

	private List<String> options = new ArrayList<>();

	@NotNull
	private String language;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CustomField.FieldType getType() {
		return type;
	}

	public void setType(CustomField.FieldType type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getOptions() {
		return options;
	}

	public void setOptions(List<String> options) {
		this.options = options;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public CustomFieldUpdateRequest buildCustomFieldUpdateRequest() {
		switch (type) {
			case SELECTBOX:
			case CHECKBOX:
			case RADIO:
				break;
			default:
				options.clear();
				break;
		}
		CustomFieldUpdateRequest.Builder builder = new CustomFieldUpdateRequest.Builder();
		return builder
				.id(id)
				.name(name)
				.description(description)
				.type(type)
				.options(options)
				.language(language)
				.build();
	}

	public static CustomFieldEditForm fromDomainObject(CustomField customField) {
		CustomFieldEditForm form = new CustomFieldEditForm();
		BeanUtils.copyProperties(customField, form);
		form.setType(customField.getFieldType());

		List<String> options = new LinkedList<>();
		customField.getOptions().stream().map(o -> o.getName()).forEach(options::add);

		return form;
	}
}
