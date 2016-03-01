package org.wallride.core.domain;


import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table
@DynamicInsert
@DynamicUpdate
@Analyzer(definition = "synonyms")
@Indexed
@SuppressWarnings("serial")
public class CustomFieldValue extends DomainObject<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@IndexedEmbedded(includeEmbeddedObjectId = true)
	@JoinColumn(name="custom_field_id", insertable = false, updatable = false)
	private CustomField customField;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@IndexedEmbedded(includeEmbeddedObjectId = true)
	@JoinColumn(name="custom_field_id")
	private Post post;

	@Column
	private String stringValue;

	@Column
	private Long numberValue;

	@Column
	@Lob
	private String textValue;

	@Column
	private LocalDate dateValue;

	@Column
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
}
