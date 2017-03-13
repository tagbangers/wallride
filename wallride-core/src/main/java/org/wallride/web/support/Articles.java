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

package org.wallride.web.support;

import org.springframework.data.domain.Page;
import org.thymeleaf.context.IExpressionContext;
import org.wallride.domain.Article;
import org.wallride.domain.Post;
import org.wallride.model.ArticleSearchRequest;
import org.wallride.support.ArticleUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Articles {

	private IExpressionContext context;
	private ArticleUtils articleUtils;

	public Articles(IExpressionContext context, ArticleUtils articleUtils) {
		this.context = context;
		this.articleUtils = articleUtils;
	}

	public List<Article> search(Condition condition) {
		Page<Article> result = articleUtils.search(condition.buildArticleSearchRequest(), condition.size);
		return new ArrayList<>(result.getContent());
	}

	public List<Article> getAllArticles() {
		return articleUtils.getAllArticles();
	}

	public Condition condition() {
		return new Condition();
	}

	class Condition {

		private int size = 1;

		private String keyword;
		private Collection<Long> categoryIds;
		private Collection<String> categoryCodes;
		private Collection<String> tagNames;
		private Long authorId;
		private Post.Status status = Post.Status.PUBLISHED;
//		private LocalDateTime dateFrom;
//		private LocalDateTime dateTo;

		public Condition size(int size) {
			this.size = size;
			return this;
		}

		public Condition keyword(String keyword) {
			this.keyword = keyword;
			return this;
		}

		public Condition category(Long... ids) {
			List<Long> categoryIds = new ArrayList<>();
			for (Long value : ids) {
				categoryIds.add(value);
			}
			this.categoryIds = categoryIds;
			return this;
		}

		public Condition category(String... codes) {
			List<String> categoryCodes = new ArrayList<>();
			for (String value : codes) {
				categoryCodes.add(value);
			}
			this.categoryCodes = categoryCodes;
			return this;
		}

		public Condition tag(String... names) {
			List<String> tagNames = new ArrayList<>();
			for (String value : names) {
				tagNames.add(value);
			}
			this.tagNames = tagNames;
			return this;
		}

		public Condition author(Long id) {
			this.authorId = id;
			return this;
		}

		private ArticleSearchRequest buildArticleSearchRequest() {
			ArticleSearchRequest request = new ArticleSearchRequest(context.getLocale().getLanguage())
					.withStatus(this.status)
					.withKeyword(this.keyword)
					.withCategoryIds(this.categoryIds)
					.withCategoryCodes(this.categoryCodes)
					.withTagNames(this.tagNames)
					.withAuthorId(this.authorId);
			return request;
		}
	}
}