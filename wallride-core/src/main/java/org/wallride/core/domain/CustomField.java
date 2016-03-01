package org.wallride.core.domain;


import org.hibernate.annotations.*;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.SortableField;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table
@DynamicInsert
@DynamicUpdate
@Analyzer(definition = "synonyms")
@Indexed
@SuppressWarnings("serial")
public class CustomField extends DomainObject<Long> {

	public enum FieldType {
		UNDEFINED, TEXT, TEXTAREA, HTML, SELECTBOX, CHECKBOX, RADIO, NUMBER, DATE, DATETIME,
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column
	@Field
	@SortableField
	private long idx;

	@Column(length = 200)
	@Field
	private String name;

	@Lob
	@Field
	private String description;

	@Field
	@Enumerated(EnumType.STRING)
	@Column(length = 50, nullable = false)
	private FieldType fieldType;

	@Column(length = 200)
	private String defaultValue;

	@Column(length = 3, nullable = false)
	@Field
	private String language;

	@ElementCollection(fetch=FetchType.LAZY)
	@JoinTable(name="custom_field_option", joinColumns=@JoinColumn(name="custom_field_id"))
	@OrderColumn(name="`idx`")
	private List<CustomFieldOption> options = new ArrayList<>();

//	@OneToMany(mappedBy = "customField", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//	private List<CustomFieldValue> customFieldValues = new ArrayList<>();

	@Override
	public Long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getIdx() {
		return idx;
	}

	public void setIdx(long idx) {
		this.idx = idx;
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

	public FieldType getFieldType() {
		return fieldType;
	}

	public void setFieldType(FieldType fieldType) {
		this.fieldType = fieldType;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public List<CustomFieldOption> getOptions() {
		return options;
	}

	public void setOptions(List<CustomFieldOption> options) {
		this.options = options;
	}

//	public List<CustomFieldValue> getCustomFieldValues() {
//		return customFieldValues;
//	}
//
//	public void setCustomFieldValues(List<CustomFieldValue> customFieldValues) {
//		this.customFieldValues = customFieldValues;
//	}
}
