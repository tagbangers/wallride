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

import org.springframework.context.i18n.LocaleContextHolder;
import org.wallride.domain.Category;
import org.wallride.model.TreeNode;
import org.wallride.service.CategoryService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class CategoryUtils {

	private CategoryService categoryService;

	public CategoryUtils(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	public List<Category> getAllCategories() {
		return getAllCategories(false);
	}

	public List<Category> getAllCategories(boolean includeNoPosts) {
		return categoryService.getCategories(LocaleContextHolder.getLocale().getLanguage(), includeNoPosts);
	}

	public List<TreeNode<Category>> getNodes() {
		return getNodes(false);
	}

	public List<TreeNode<Category>> getNodes(boolean includeNoPosts) {
		Collection<Category> categories = categoryService.getCategories(LocaleContextHolder.getLocale().getLanguage(), includeNoPosts);

		List<TreeNode<Category>> rootNodes = new ArrayList<>();
		Iterator<Category> i = categories.iterator();
		while (i.hasNext()) {
			Category Category = i.next();
			if (Category.getParent() == null) {
				TreeNode<Category> node = new TreeNode<>(Category);
				rootNodes.add(node);
				i.remove();
			}
		}

		for (TreeNode<Category> node : rootNodes) {
			createNode(node, categories);
		}
		return rootNodes;
	}

	private void createNode(TreeNode<Category> parent, Collection<Category> Categories) {
		List<TreeNode<Category>> children = new ArrayList<>();
		Iterator<Category> i = Categories.iterator();
		while (i.hasNext()) {
			Category Category = i.next();
			TreeNode<Category> node = new TreeNode<>(Category);
			node.setParent(parent);
			if (parent.getObject().equals(Category.getParent())) {
				children.add(node);
				i.remove();
			}
		}
		parent.setChildren(children);

		for (TreeNode<Category> node : children) {
			createNode(node, Categories);
		}
	}
}
