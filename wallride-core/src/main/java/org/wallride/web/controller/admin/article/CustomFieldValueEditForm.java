package org.wallride.web.controller.admin.article;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.util.StringUtils;
import org.wallride.domain.CustomField;
import org.wallride.domain.CustomFieldOption;
import org.wallride.domain.CustomFieldValue;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CustomFieldValueEditForm implements Serializable {

	private Long id;
	@NotNull
	private long customFieldId;
	@NotNull
	private String name;
	private String description;
	private CustomField.FieldType fieldType;
	private String stringValue;
	private String[] stringValues;
	private String textValue;
	@NumberFormat
	private Long numberValue;
	@DateTimeFormat(pattern = "yyyy/MM/dd")
	private LocalDate dateValue;
	@DateTimeFormat(pattern = "yyyy/MM/dd HH:mm")
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

	public String[] getStringValues() {
		return stringValues;
	}

	public void setStringValues(String[] stringValues) {
		this.stringValues = stringValues;
	}

	public String getTextValue() {
		return textValue;
	}

	public void setTextValue(String textValue) {
		this.textValue = textValue;
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

	public static CustomFieldValueEditForm fromDomainObject(CustomFieldValue fieldValue) {
		CustomFieldValueEditForm form = new CustomFieldValueEditForm();
		form.setId(fieldValue.getId());
		form.setCustomFieldId(fieldValue.getCustomField().getId());
		form.setName(fieldValue.getCustomField().getName());
		form.setDescription(fieldValue.getCustomField().getDescription());
		form.setFieldType(fieldValue.getCustomField().getFieldType());
		if (fieldValue.getCustomField().getFieldType().equals(CustomField.FieldType.CHECKBOX)) {
			if (fieldValue.getStringValue() != null) {
				form.setStringValues(fieldValue.getStringValue().split(","));
			}
		} else {
			form.setStringValue(fieldValue.getStringValue());
		}
		form.setTextValue(fieldValue.getTextValue());
		form.setNumberValue(fieldValue.getNumberValue());
		form.setDateValue(fieldValue.getDateValue());
		form.setDatetimeValue(fieldValue.getDatetimeValue());
		form.setOptions(fieldValue.getCustomField().getOptions());
		return form;
	}

	public boolean isEmpty() {
		switch (getFieldType()) {
			case TEXT:
			case SELECTBOX:
			case RADIO:
				if (StringUtils.isEmpty(getStringValue())) {
					return true;
				}
				return false;
			case CHECKBOX:
				if (getStringValues().length == 0) {
					return true;
				}
				return false;
			case TEXTAREA:
			case HTML:
				if (StringUtils.isEmpty(getTextValue())) {
					return true;
				}
				return false;
			case DATE:
				if (getDateValue() == null) {
					return true;
				}
				return false;
			case DATETIME:
				if (getDatetimeValue() == null) {
					return true;
				}
				return false;
			case NUMBER:
				if (getNumberValue() == null) {
					return true;
				}
				return false;
			default:
				return true;
		}
	}
}
