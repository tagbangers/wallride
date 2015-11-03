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

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.wallride.core.domain.Article;
import org.wallride.core.domain.Category;
import org.wallride.core.domain.Post;
import org.wallride.core.domain.Tag;
import org.wallride.core.service.ArticleUpdateRequest;
import org.wallride.web.support.DomainObjectEditForm;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("serial")
public class ArticleEditForm extends DomainObjectEditForm {

	interface GroupPublish {}

	@NotNull
	private Long id;

	private String code;

	private String coverId;

	@NotNull(groups=GroupPublish.class)
	private String title;

	@NotNull(groups=GroupPublish.class)
	private String body;

	private Long authorId;

	@DateTimeFormat(pattern="yyyy/MM/dd HH:mm")
	private LocalDateTime date;

	private Set<Long> categoryIds = new HashSet<>();
	private String tags;
	private Set<Long> relatedPostIds = new HashSet<>();

	private String seoTitle;
	private String seoDescription;
	private String seoKeywords;

	@NotNull
	private String language;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCoverId() {
		return coverId;
	}

	public void setCoverId(String coverId) {
		this.coverId = coverId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Long getAuthorId() {
		return authorId;
	}

	public void setAuthorId(Long authorId) {
		this.authorId = authorId;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public Set<Long> getCategoryIds() {
		return categoryIds;
	}

	public void setCategoryIds(Set<Long> categoryIds) {
		this.categoryIds = categoryIds;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public Set<Long> getRelatedPostIds() {
		return relatedPostIds;
	}

	public void setRelatedPostIds(Set<Long> relatedPostIds) {
		this.relatedPostIds = relatedPostIds;
	}

	public String getSeoTitle() {
		return seoTitle;
	}

	public void setSeoTitle(String seoTitle) {
		this.seoTitle = seoTitle;
	}

	public String getSeoDescription() {
		return seoDescription;
	}

	public void setSeoDescription(String seoDescription) {
		this.seoDescription = seoDescription;
	}

	public String getSeoKeywords() {
		return seoKeywords;
	}

	public void setSeoKeywords(String seoKeywords) {
		this.seoKeywords = seoKeywords;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public ArticleUpdateRequest buildArticleUpdateRequest() {
		ArticleUpdateRequest.Builder builder = new ArticleUpdateRequest.Builder();
		return builder
				.id(id)
				.code(code)
				.coverId(coverId)
				.title(title)
				.body(body)
				.authorId(authorId)
				.date(date)
				.categoryIds(categoryIds)
				.tags(tags)
				.relatedPostIds(relatedPostIds)
				.seoTitle(seoTitle)
				.seoDescription(seoDescription)
				.seoKeywords(seoKeywords)
				.language(language)
				.build();
	}

	public static ArticleEditForm fromDomainObject(Article article) {
		ArticleEditForm form = new ArticleEditForm();
		BeanUtils.copyProperties(article, form);

		if (article.getStatus().equals(Post.Status.DRAFT)) {
			form.setCode(article.getDraftedCode());
		}

		form.setCoverId(article.getCover() != null ? article.getCover().getId() : null);

		for (Category category : article.getCategories()) {
			form.getCategoryIds().add(category.getId());
		}

		List<String> tagNames = new ArrayList<>();
		for (Tag tag : article.getTags()) {
			tagNames.add(tag.getName());
		}
		form.setTags(StringUtils.join(tagNames, ","));

		for (Post post : article.getRelatedToPosts()) {
			form.getRelatedPostIds().add(post.getId());
		}

		if (article.getSeo() != null) {
			form.setSeoTitle(article.getSeo().getTitle());
			form.setSeoDescription(article.getSeo().getDescription());
			form.setSeoKeywords(article.getSeo().getKeywords());
		}
		return form;
	}
}
