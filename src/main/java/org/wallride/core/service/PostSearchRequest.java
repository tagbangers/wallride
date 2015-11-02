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

package org.wallride.core.service;

import java.time.LocalDateTime;
import org.wallride.core.domain.Post;

import java.util.ArrayList;
import java.util.Collection;

public class PostSearchRequest {

	private Collection<Long> postIds = new ArrayList<>();
	private Post.Status status = Post.Status.PUBLISHED;
	private String keyword;
	private Collection<String> tagNames = new ArrayList<>();
	private LocalDateTime dateFrom;
	private LocalDateTime dateTo;
	private String language;

	public PostSearchRequest(String language) {
		this.language = language;
	}

	public Collection<Long> getPostIds() {
		return postIds;
	}

	public void setPostIds(Collection<Long> postIds) {
		this.postIds = postIds;
	}

	public PostSearchRequest withPostIds(Long... postIds) {
		if (getPostIds() == null) {
			setPostIds(new ArrayList<Long>(postIds.length));
		}
		for (Long value : postIds) {
			getPostIds().add(value);
		}
		return this;
	}

	public Post.Status getStatus() {
		return status;
	}

	public void setStatus(Post.Status status) {
		this.status = status;
	}
	
	public PostSearchRequest withStatus(Post.Status status) {
		this.status = status;
		return this;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public PostSearchRequest withKeyword(String keyword) {
		this.keyword = keyword;
		return this;
	}

	public Collection<String> getTagNames() {
		return tagNames;
	}

	public void setTagNames(Collection<String> tagNames) {
		this.tagNames = tagNames;
	}

	public PostSearchRequest withTagNames(String... tagNames) {
		if (getTagNames() == null) {
			setTagNames(new ArrayList<String>(tagNames.length));
		}
		for (String value : tagNames) {
			getTagNames().add(value);
		}
		return this;
	}

	public LocalDateTime getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(LocalDateTime dateFrom) {
		this.dateFrom = dateFrom;
	}

	public PostSearchRequest withDateFrom(LocalDateTime dateFrom) {
		this.dateFrom = dateFrom;
		return this;
	}

	public LocalDateTime getDateTo() {
		return dateTo;
	}

	public void setDateTo(LocalDateTime dateTo) {
		this.dateTo = dateTo;
	}

	public PostSearchRequest withDateTo(LocalDateTime dateTo) {
		this.dateTo = dateTo;
		return this;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public PostSearchRequest withLanguage(String language) {
		this.language = language;
		return this;
	}
}
