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

package org.wallride.core.domain;

import java.io.Serializable;
import java.util.*;

public class CategoryTree implements Serializable {

	private HashMap<Long, Category> categoryIdMap = new LinkedHashMap<>();

	private HashMap<String, Category> categoryCodeMap = new LinkedHashMap<>();

	private ArrayList<Node> rootNodes = new ArrayList<>();

	public CategoryTree(Collection<Category> categories) {
		categories = new TreeSet<>(categories);
		for (Category category : categories) {
			categoryIdMap.put(category.getId(), category);
			categoryCodeMap.put(category.getCode(), category);
		}

		Iterator<Category> i = categories.iterator();
		while (i.hasNext()) {
			Category category = i.next();
			if (category.getParent() == null) {
				rootNodes.add(new Node(category));
				i.remove();
			}
		}

		for (Node node : rootNodes) {
			createChildren(node, categories);
		}
	}

	private void createChildren(Node parent, Collection<Category> categories) {
		ArrayList<Node> children = new ArrayList<>();
		Iterator<Category> i = categories.iterator();
		while (i.hasNext()) {
			Category category = i.next();
			Node node = new Node(category);
			if (parent.getCategory().equals(category.getParent())) {
				children.add(node);
				i.remove();
//				createChildren(node, categories);
			}
		}
		parent.children = children;

		for (Node node : children) {
			createChildren(node, categories);
		}
	}

	public List<Node> getRootNodes() {
		return rootNodes;
	}

	public Collection<Category> getCategories() {
		return categoryIdMap.values();
	}

	public Category getCategoryById(long id) {
		return categoryIdMap.get(id);
	}

	public Category getCategoryByCode(String code) {
		return categoryCodeMap.get(code);
	}

	public boolean isEmpty() {
		return categoryIdMap.isEmpty();
	}

	public static class Node implements Serializable {

		private Category category;

		private ArrayList<Node> children = new ArrayList<>();

		private Node(Category category) {
			this.category = category;
		}

		public Category getCategory() {
			return category;
		}

		public List<Node> getChildren() {
			return children;
		}
	}
}
