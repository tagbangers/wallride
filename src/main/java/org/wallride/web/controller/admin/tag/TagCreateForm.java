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
