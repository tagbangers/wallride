package org.wallride.domain;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.solr.analysis.*;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.*;
import org.joda.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

@MappedSuperclass
@AnalyzerDef(name = "synonyms",
tokenizer = 
	@TokenizerDef(factory = JapaneseTokenizerFactory.class, params=
		@Parameter(name="userDictionary", value="userdict.txt")),
filters = {
	@TokenFilterDef(factory=SynonymFilterFactory.class, params = {
		@Parameter(name="synonyms", value="synonyms.txt"),
		@Parameter(name="userDictionary", value="userdict.txt"),
		@Parameter(name="ignoreCase", value="true"),
		@Parameter(name="expand", value="true"),
		@Parameter(name="tokenizerFactory", value="org.apache.solr.analysis.JapaneseTokenizerFactory")}),
	@TokenFilterDef(factory=JapaneseBaseFormFilterFactory.class),
	@TokenFilterDef(factory=CJKWidthFilterFactory.class),
	@TokenFilterDef(factory=JapaneseKatakanaStemFilterFactory.class, params={
		@Parameter(name="minimumLength", value="4")}),
	@TokenFilterDef(factory=LowerCaseFilterFactory.class)
})
@SuppressWarnings("serial")
public abstract class DomainObject<ID extends Serializable> implements Serializable {

	@Column(name="created_at", nullable=false)
	@Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
	@FieldBridge(impl=LocalDateTimeBridge.class)
	private LocalDateTime createdAt = new LocalDateTime();
	
	@Column(name="created_by", length=100)
	private String createdBy;
	
	@Column(name="updated_at", nullable=false)
	@Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
	@FieldBridge(impl=LocalDateTimeBridge.class)
	private LocalDateTime updatedAt = new LocalDateTime();
	
	@Column(name="updated_by", length=100)
	private String updatedBy;

	public abstract ID getId();

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (other == null || !(other instanceof DomainObject)) return false;
		DomainObject that = (DomainObject) other;
		return (getId() == that.getId());
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(getId()).toHashCode();
	}
}
