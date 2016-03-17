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

import org.thymeleaf.context.IProcessingContext;
import org.wallride.domain.*;
import org.wallride.support.ArticleUtils;

import java.util.ArrayList;
import java.util.List;

public class Articles {

	private IProcessingContext processingContext;

	private ArticleUtils articleUtils;

	public Articles(IProcessingContext processingContext, ArticleUtils articleUtils) {
		this.processingContext = processingContext;
		this.articleUtils = articleUtils;
	}

	public List<Article> byCategoryAndTagName(Category category, String tagName, int size) {
		org.springframework.data.domain.Page<Article> articles = articleUtils.byCategoryAndTag(category.getCode(), tagName, size);
		return new ArrayList<>(articles.getContent());
	}
}