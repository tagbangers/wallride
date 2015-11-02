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

package org.wallride.core.repository;

import java.time.LocalDateTime;
import org.wallride.core.domain.Post;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("serial")
public class PostFullTextSearchTerm implements Serializable {

	private Post.Status status = Post.Status.PUBLISHED;

	private String keyword;

	private LocalDateTime dateFrom;

	private LocalDateTime dateTo;

	private Collection<Long> categoryIds = new ArrayList<>();

	private Collection<Long> tagIds = new ArrayList<>();

	private String language;

	public Post.Status getStatus() {
		return status;
	}

	public void setStatus(Post.Status status) {
		this.status = status;
	}

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

	public Collection<Long> getTagIds() {
		return tagIds;
	}

	public void setTagIds(Collection<Long> tagIds) {
		this.tagIds = tagIds;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
}
