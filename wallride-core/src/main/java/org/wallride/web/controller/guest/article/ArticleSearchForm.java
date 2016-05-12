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

package org.wallride.web.controller.guest.article;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.springframework.util.StringUtils;
import org.wallride.domain.Post;
import org.wallride.model.ArticleSearchRequest;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class ArticleSearchForm implements Serializable {

	private String keyword;
	private LocalDateTime dateFrom;
	private LocalDateTime dateTo;
	private Collection<Long> categoryIds = new ArrayList<>();
	private Collection<String> categoryCodes = new ArrayList<>();
	private Collection<String> tagNames = new ArrayList<>();
	private Map<String, Object> customFields = new HashMap<>();
	private Long authorId;
	private String language;

	public String getKeyword() {
		return keyword;
	}
	
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public LocalDateTime getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(LocalDateTime dateFrom) {
		this.dateFrom = dateFrom;
	}

	public LocalDateTime getDateTo() {
		return dateTo;
	}

	public void setDateTo(LocalDateTime dateTo) {
		this.dateTo = dateTo;
	}

	public Collection<Long> getCategoryIds() {
		return categoryIds;
	}

	public void setCategoryIds(Collection<Long> categoryIds) {
		this.categoryIds = categoryIds;
	}

	public Collection<String> getCategoryCodes() {
		return categoryCodes;
	}

	public void setCategoryCodes(Collection<String> categoryCodes) {
		this.categoryCodes = categoryCodes;
	}

	public Collection<String> getTagNames() {
		return tagNames;
	}

	public void setTagNames(Collection<String> tagNames) {
		this.tagNames = tagNames;
	}

	public Map<String, Object> getCustomFields() {
		return customFields;
	}

	public void setCustomFields(Map<String, Object> customFields) {
		this.customFields = customFields;
	}

	public Long getAuthorId() {
		return authorId;
	}

	public void setAuthorId(Long authorId) {
		this.authorId = authorId;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public boolean isEmpty() {
		if (StringUtils.hasText(getKeyword())) {
			return false;
		}
		return true;
	}

	public ArticleSearchRequest toArticleSearchRequest() {
		ArticleSearchRequest request = new ArticleSearchRequest();
		request
				.withKeyword(getKeyword())
				.withDateFrom(getDateFrom())
				.withDateTo(getDateTo())
				.withCategoryIds(getCategoryIds())
				.withCategoryCodes(getCategoryCodes())
				.withTagNames(getTagNames())
				.withCustomFields(getCustomFields())
				.withAuthorId(getAuthorId())
				.withLanguage(getLanguage())
				.withStatus(Post.Status.PUBLISHED);
		return request;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (other == null || !(other instanceof ArticleSearchForm)) return false;
		ArticleSearchForm that = (ArticleSearchForm) other;
		return new EqualsBuilder()
				.append(getKeyword(), that.getKeyword())
				.append(getDateFrom(), that.getDateFrom())
				.append(getDateTo(), that.getDateTo())
				.append(getCategoryIds(), that.getCategoryIds())
				.append(getCategoryCodes(), that.getCategoryCodes())
				.append(getTagNames(), that.getTagNames())
				.append(getCustomFields(), that.getCustomFields())
				.append(getAuthorId(), that.getAuthorId())
				.append(getLanguage(), that.getLanguage())
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(getKeyword())
				.append(getDateFrom())
				.append(getDateTo())
				.append(getCategoryIds())
				.append(getCategoryCodes())
				.append(getTagNames())
				.append(getCustomFields())
				.append(getAuthorId())
				.append(getLanguage())
				.toHashCode();
	}
}
