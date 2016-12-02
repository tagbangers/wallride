package org.wallride.domain;


import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SortNatural;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.SortableField;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

@Entity
@NamedEntityGraphs({
		@NamedEntityGraph(name = CustomField.SHALLOW_GRAPH_NAME),
		@NamedEntityGraph(name = CustomField.DEEP_GRAPH_NAME,
				attributeNodes = {
						@NamedAttributeNode("options")})
})
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"code", "language"}))
@DynamicInsert
@DynamicUpdate
@Indexed
@SuppressWarnings("serial")
public class CustomField extends DomainObject<Long> implements Comparable<CustomField> {

	public static final String SHALLOW_GRAPH_NAME = "CUSTOM_FIELD_SHALLOW_GRAPH";
	public static final String DEEP_GRAPH_NAME = "CUSTOM_FIELD_DEEP_GRAPH";

	public static final String STRING_VALUE = "stringValue";
	public static final String TEXT_VALUE = "textValue";
	public static final String NUMBER_VALUE = "numberValue";
	public static final String DATE_VALUE = "dateValue";
	public static final String DATETIME_VALUE = "datetimeValue";

	public enum FieldType {
		UNDEFINED(null),
		TEXT(STRING_VALUE),
		TEXTAREA(TEXT_VALUE),
		HTML(TEXT_VALUE),
		SELECTBOX(STRING_VALUE),
		CHECKBOX(TEXT_VALUE),
		RADIO(STRING_VALUE),
		NUMBER(NUMBER_VALUE),
		DATE(DATE_VALUE),
		DATETIME(DATETIME_VALUE);

		private String valueType;

		FieldType(String valueType) {
			this.valueType = valueType;
		}

		public String getValueType() {
			return valueType;
		}
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Field(name = "sortId", analyze = Analyze.NO, index = org.hibernate.search.annotations.Index.NO)
	@SortableField(forField = "sortId")
	private long id;

	@Column
	@Field
	@SortableField
	private Integer idx;

	@Column(length = 200)
	@Field(analyze = Analyze.NO)
	private String code;

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

	@OneToMany(mappedBy = "customField", cascade = CascadeType.ALL)
	@SortNatural
	private SortedSet<CustomFieldValue> customFieldValues = new TreeSet<>();

	@ElementCollection(fetch=FetchType.LAZY)
	@JoinTable(name="custom_field_option", joinColumns=@JoinColumn(name="custom_field_id"))
	@OrderColumn(name="`idx`")
	private List<CustomFieldOption> options = new ArrayList<>();

	@Override
	public Long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Integer getIdx() {
		return idx;
	}

	public void setIdx(Integer idx) {
		this.idx = idx;
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

	public SortedSet<CustomFieldValue> getCustomFieldValues() {
		return customFieldValues;
	}

	public void setCustomFieldValues(SortedSet<CustomFieldValue> customFieldValues) {
		this.customFieldValues = customFieldValues;
	}

	public List<CustomFieldOption> getOptions() {
		return options;
	}

	public void setOptions(List<CustomFieldOption> options) {
		this.options = options;
	}

	@Override
	public String print() {
		return getName();
	}

	@Override
	public int compareTo(CustomField field) {
		return new CompareToBuilder()
				.append(getIdx(), field.getIdx())
				.append(getId(), field.getId())
				.toComparison();
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) { return false; }
		if (other == this) { return true; }
		if (other.getClass() != getClass()) { return false; }
		CustomField customField = (CustomField) other;
		return new EqualsBuilder()
				.append(getId(), (customField.getId()))
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(getId())
				.toHashCode();
	}

	@Override
	public String toString() {
		return getName();
	}
}