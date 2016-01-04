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
import org.wallride.core.domain.Category;
import org.wallride.core.model.TreeNode;
import org.wallride.core.support.CategoryUtils;

import java.util.List;

public class Categorys {

	private IProcessingContext processingContext;

	private CategoryUtils CategoryUtils;

	public Categorys(IProcessingContext processingContext, CategoryUtils CategoryUtils) {
		this.processingContext = processingContext;
		this.CategoryUtils = CategoryUtils;
	}

	public List<Category> getAllCategories() {
		return CategoryUtils.getAllCategories();
	}

	public List<Category> getAllCategories(boolean includeNoArticle) {
		return CategoryUtils.getAllCategories(includeNoArticle);
	}

	public List<TreeNode<Category>> getNodes() {
		return CategoryUtils.getNodes();
	}

	public List<TreeNode<Category>> getNodes(boolean includeNoArticle) {
		return CategoryUtils.getNodes(includeNoArticle);
	}
}