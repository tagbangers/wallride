package org.wallride.domain;


import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "custom_field_id"}))
@DynamicInsert
@DynamicUpdate
@Analyzer(definition = "synonyms")
@Indexed
@SuppressWarnings("serial")
public class CustomFieldValue extends DomainObject<Long> implements Comparable<CustomFieldValue> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@IndexedEmbedded(includeEmbeddedObjectId = true)
	@JoinColumn(name="custom_field_id")
	private CustomField customField;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@IndexedEmbedded(includeEmbeddedObjectId = true)
	private Post post;

	@Column(length = 300)
	@Field
	private String stringValue;

	@Column
	@Field
	private Long numberValue;

	@Column
	@Lob
	@Field
	private String textValue;

	@Column
	@Field
	private LocalDate dateValue;

	@Column
	@Field
	private LocalDateTime datetimeValue;

	@Override
	public Long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public CustomField getCustomField() {
		return customField;
	}

	public void setCustomField(CustomField customField) {
		this.customField = customField;
	}

	public Post getPost() {
		return post;
	}

	public void setPost(Post post) {
		this.post = post;
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

	public String getTextValue() {
		return textValue;
	}

	public void setTextValue(String textValue) {
		this.textValue = textValue;
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

	public Object getValue() {
		switch (getCustomField().getFieldType()) {
			case TEXT:
			case SELECTBOX:
			case RADIO:
			case CHECKBOX:
				return getStringValue();
			case TEXTAREA:
			case HTML:
				return getTextValue();
			case DATE:
				return getDateValue();
			case DATETIME:
				return getDatetimeValue();
			case NUMBER:
				return getNumberValue();
			default:
				return null;
		}
	}

	public boolean isEmpty() {
		switch (getCustomField().getFieldType()) {
			case TEXT:
			case SELECTBOX:
			case RADIO:
			case CHECKBOX:
				if (StringUtils.isEmpty(getStringValue())) {
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

	@Override
	public int compareTo(CustomFieldValue customFieldValue) {
		if (getId() == 0) {
			return 1;
		}
		int fieldDiff = getCustomField().compareTo(customFieldValue.getCustomField());
		if (fieldDiff != 0) {
			return fieldDiff;
		}
		return Long.compare(getId(), customFieldValue.getId());
	}

	@Override
	public String toString() {
		return this.getClass().getName() + " " + getId();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (other == null || !(other instanceof CustomFieldValue)) return false;
		if (getId() == 0) return false;
		CustomFieldValue that = (CustomFieldValue) other;
		return getId() == that.getId();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(getId()).toHashCode();
	}
}
