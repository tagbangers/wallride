package org.wallride.core.domain;


import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.SortableField;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@NamedEntityGraphs({
		@NamedEntityGraph(name = CustomField.SHALLOW_GRAPH_NAME,
				attributeNodes = {
						@NamedAttributeNode("options")}
		),
		@NamedEntityGraph(name = CustomField.DEEP_GRAPH_NAME,
				attributeNodes = {
						@NamedAttributeNode("options")})
})
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name", "language"}))
@DynamicInsert
@DynamicUpdate
@Indexed
@SuppressWarnings("serial")
public class CustomField extends DomainObject<Long> implements Comparable<CustomField> {

	public static final String SHALLOW_GRAPH_NAME = "CUSTOM_FIELD_SHALLOW_GRAPH";
	public static final String DEEP_GRAPH_NAME = "CUSTOM_FIELD_DEEP_GRAPH";

	public enum FieldType {
		UNDEFINED, TEXT, TEXTAREA, HTML, SELECTBOX, CHECKBOX, RADIO, NUMBER, DATE, DATETIME,
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(unique = true)
	@Field
	@SortableField
	private int idx;

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

	@Override
	public Long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
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

	@Override
	public int compareTo(CustomField field) {
		return new CompareToBuilder()
				.append(getIdx(), field.getIdx())
				.append(getId(), field.getId())
				.toComparison();
	}

	@Override
	public String toString() {
		return getName();
	}
}
