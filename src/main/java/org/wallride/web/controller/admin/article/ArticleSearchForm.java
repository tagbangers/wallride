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

package org.wallride.web.controller.admin.article;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.wallride.core.domain.Post;
import org.wallride.core.service.ArticleSearchRequest;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ArticleSearchForm implements Serializable {
	
	private String keyword;
	private Long categoryId;
	private Long tagId;
	private Long authorId;
	private Post.Status status;

	public String getKeyword() {
		return keyword;
	}
	
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	public Long getTagId() {
		return tagId;
	}

	public void setTagId(Long tagId) {
		this.tagId = tagId;
	}

	public Long getAuthorId() {
		return authorId;
	}

	public void setAuthorId(Long authorId) {
		this.authorId = authorId;
	}

	public Post.Status getStatus() {
		return status;
	}

	public void setStatus(Post.Status status) {
		this.status = status;
	}

	public boolean isEmpty() {
		if (StringUtils.hasText(getKeyword())) {
			return false;
		}
		if (getStatus() != null) {
			return false;
		}
		return true;
	}
	
	public boolean isAdvanced() {
		return false;
	}

	public ArticleSearchRequest toArticleSearchRequest() {
		ArticleSearchRequest request = new ArticleSearchRequest();
		request.setKeyword(getKeyword());
		if (getCategoryId() != null) {
			request.withCategoryIds(getCategoryId());
		}
		if (getTagId() != null) {
			request.withTagIds(getTagId());
		}
		request.setAuthorId(getAuthorId());
		request.setStatus(getStatus());
		request.setLanguage(LocaleContextHolder.getLocale().getLanguage());
		return request;
	}

	public MultiValueMap<String, String> toQueryParams() {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		if (StringUtils.hasText(keyword)) {
			params.add("keyword", keyword);
		}
		if (categoryId != null) {
			params.add("categoryId", Long.toString(categoryId));
		}
		if (tagId != null) {
			params.add("tagId", Long.toString(tagId));
		}
		if (authorId != null) {
			params.add("authorId", Long.toString(authorId));
		}
		if (status != null) {
			params.add("status", status.toString());
		}
		return params;
	}
}
