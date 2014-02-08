package org.wallride.web.controller.admin.category;

import org.springframework.beans.BeanUtils;
import org.wallride.core.domain.Category;
import org.wallride.core.service.CategoryCreateRequest;
import org.wallride.web.support.DomainObjectCreateForm;

import javax.validation.constraints.NotNull;

@SuppressWarnings("serial")
public class CategoryCreateForm extends DomainObjectCreateForm {

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
