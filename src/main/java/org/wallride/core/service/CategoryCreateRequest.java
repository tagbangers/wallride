package org.wallride.core.service;

import org.springframework.beans.BeanUtils;
import org.wallride.core.domain.Category;
import org.wallride.core.web.DomainObjectCreateForm;

import javax.validation.constraints.NotNull;

@SuppressWarnings("serial")
public class CategoryCreateRequest extends DomainObjectCreateForm {

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

	public static CategoryCreateRequest fromDomainObject(Category category) {
		CategoryCreateRequest form = new CategoryCreateRequest();
		BeanUtils.copyProperties(category, form);
		return form;
	}
}
