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

package org.wallride.support;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.wallride.domain.Article;
import org.wallride.model.ArticleSearchRequest;
import org.wallride.service.ArticleService;

public class ArticleUtils {

	private ArticleService articleService;

	public ArticleUtils(ArticleService articleService) {
		this.articleService = articleService;
	}

	public Page<Article> byCategoryAndTag(String categoryCode, String tagName, int size) {
		ArticleSearchRequest request = new ArticleSearchRequest()
				.withCategoryCodes(categoryCode)
				.withTagNames(tagName);
		return articleService.getArticles(request, new PageRequest(0, size));
	}
}
