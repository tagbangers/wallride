package org.wallride.web.controller.admin.tag;

import org.springframework.beans.BeanUtils;
import org.wallride.core.domain.Category;
import org.wallride.core.service.TagUpdateRequest;
import org.wallride.web.support.DomainObjectEditForm;

import javax.validation.constraints.NotNull;

@SuppressWarnings("serial")
public class TagEditForm extends DomainObjectEditForm {

	@NotNull
	private Long id;
	@NotNull
	private String name;
	@NotNull
	private String language;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public TagUpdateRequest buildTagUpdateRequest() {
		TagUpdateRequest.Builder builder = new TagUpdateRequest.Builder();
		return builder
				.id(id)
				.name(name)
				.language(language)
				.build();
	}

	public static TagEditForm fromDomainObject(Category category) {
		TagEditForm form = new TagEditForm();
		BeanUtils.copyProperties(category, form);
		return form;
	}
}
