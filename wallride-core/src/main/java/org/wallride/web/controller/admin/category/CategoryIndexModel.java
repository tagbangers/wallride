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

package org.wallride.web.controller.admin.category;

import org.wallride.core.domain.Category;
import org.wallride.core.model.TreeNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CategoryIndexModel extends ArrayList<Map<String, Object>> {

	public CategoryIndexModel(List<TreeNode<Category>> nodes) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

		for (TreeNode<Category> node : nodes) {
			result.add(createValue(node));
		}
		this.addAll(result);
	}

	private Map<String, Object> createValue(TreeNode<Category> node) {
		Map<String, Object> parent = new LinkedHashMap<>();
		parent.put("id", node.getObject().getId());
		parent.put("code", node.getObject().getCode());
		parent.put("name", node.getObject().getName());
//		parent.put("articleCount", category.getArticleCount());

		List<Map<String, Object>> children = new ArrayList<Map<String, Object>>();
		for (TreeNode<Category> child : node.getChildren()) {
			children.add(createValue(child));
		}
		parent.put("children", children);
		return parent;
	}
}
