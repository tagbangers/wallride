package org.wallride.web.controller.admin.article;

import org.wallride.core.domain.CustomField;
import org.wallride.core.domain.CustomFieldOption;
import org.wallride.core.domain.CustomFieldValue;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CustomFieldValueEditForm implements Serializable {

	private Long id;
	private long customFieldId;
	private String name;
	private String description;
	private CustomField.FieldType fieldType;
	//	private Object value;
	private String stringValue;
	private Long numberValue;
	private LocalDate dateValue;
	private LocalDateTime datetimeValue;
	private List<CustomFieldOption> options = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public long getCustomFieldId() {
		return customFieldId;
	}

	public void setCustomFieldId(long customFieldId) {
		this.customFieldId = customFieldId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CustomField.FieldType getFieldType() {
		return fieldType;
	}

	public void setFieldType(CustomField.FieldType fieldType) {
		this.fieldType = fieldType;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public Long getNumberValue() {
		return numberValue;
	}

	public void setNumberValue(Long numberValue) {
		this.numberValue = numberValue;
	}

	public LocalDate getDateValue() {
		return dateValue;
	}

	public void setDateValue(LocalDate dateValue) {
		this.dateValue = dateValue;
	}

	public LocalDateTime getDatetimeValue() {
		return datetimeValue;
	}

	public void setDatetimeValue(LocalDateTime datetimeValue) {
		this.datetimeValue = datetimeValue;
	}

	public List<CustomFieldOption> getOptions() {
		return options;
	}

	public void setOptions(List<CustomFieldOption> options) {
		this.options = options;
	}

	public static CustomFieldValueEditForm fromDomainObject(CustomFieldValue customFieldValue) {
		CustomFieldValueEditForm form = new CustomFieldValueEditForm();
		form.setId(customFieldValue.getId());
		form.setCustomFieldId(customFieldValue.getCustomField().getId());
		form.setName(customFieldValue.getCustomField().getName());
		form.setDescription(customFieldValue.getCustomField().getDescription());
		form.setFieldType(customFieldValue.getCustomField().getFieldType());
		form.setStringValue(customFieldValue.getStringValue());
		form.setNumberValue(customFieldValue.getNumberValue());
		form.setDateValue(customFieldValue.getDateValue());
		form.setDatetimeValue(customFieldValue.getDatetimeValue());
		form.setOptions(customFieldValue.getCustomField().getOptions());
		return form;
	}
}
