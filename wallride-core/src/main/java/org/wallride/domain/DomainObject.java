/*
 * Copyright 2014 Tagbangers, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wallride.domain;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.lucene.analysis.cjk.CJKWidthFilterFactory;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.ja.JapaneseBaseFormFilterFactory;
import org.apache.lucene.analysis.ja.JapaneseKatakanaStemFilterFactory;
import org.apache.lucene.analysis.ja.JapaneseTokenizerFactory;
import org.apache.lucene.analysis.synonym.SynonymFilterFactory;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.Parameter;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
@AnalyzerDef(name = "synonyms",
		tokenizer =
		@TokenizerDef(factory = JapaneseTokenizerFactory.class, params =
		@Parameter(name = "userDictionary", value = "userdict.txt")),
		filters = {
				@TokenFilterDef(factory = SynonymFilterFactory.class, params = {
						@Parameter(name = "synonyms", value = "synonyms.txt"),
						@Parameter(name = "userDictionary", value = "userdict.txt"),
						@Parameter(name = "ignoreCase", value = "true"),
						@Parameter(name = "expand", value = "true"),
						@Parameter(name = "tokenizerFactory", value = "org.apache.lucene.analysis.ja.JapaneseTokenizerFactory")}),
				@TokenFilterDef(factory = JapaneseBaseFormFilterFactory.class),
				@TokenFilterDef(factory = CJKWidthFilterFactory.class),
				@TokenFilterDef(factory = JapaneseKatakanaStemFilterFactory.class, params = {
						@Parameter(name = "minimumLength", value = "4")}),
				@TokenFilterDef(factory = LowerCaseFilterFactory.class)
		})
@SuppressWarnings("serial")
public abstract class DomainObject<ID extends Serializable> implements Serializable {

	@Column(nullable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(length = 100)
	private String createdBy;

	@Column(nullable = false)
	private LocalDateTime updatedAt = LocalDateTime.now();

	@Column(length = 100)
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
		return new EqualsBuilder().append(getId(), that.getId()).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(getId()).toHashCode();
	}
}
