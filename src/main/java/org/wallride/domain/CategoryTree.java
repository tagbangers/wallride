package org.wallride.domain;

import java.io.Serializable;
import java.util.*;

public class CategoryTree implements Serializable {

	private Map<Long, Category> categoryIdMap = new LinkedHashMap<>();

	private Map<String, Category> categoryCodeMap = new LinkedHashMap<>();

	private List<Node> rootNodes = new ArrayList<>();

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
		List<Node> children = new ArrayList<>();
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

	public class Node {

		private Category category;

		private List<Node> children = new ArrayList<>();

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
