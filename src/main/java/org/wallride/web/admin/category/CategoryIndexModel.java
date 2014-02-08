package org.wallride.web.admin.category;

import org.wallride.domain.CategoryTree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CategoryIndexModel extends ArrayList<Map<String, Object>> {

	public CategoryIndexModel(CategoryTree categoryTree) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

		for (CategoryTree.Node node : categoryTree.getRootNodes()) {
			result.add(createValue(node));
		}
		this.addAll(result);
	}

	private Map<String, Object> createValue(CategoryTree.Node node) {
		Map<String, Object> parent = new LinkedHashMap<>();
		parent.put("id", node.getCategory().getId());
		parent.put("code", node.getCategory().getCode());
		parent.put("name", node.getCategory().getName());
//		parent.put("articleCount", category.getArticleCount());

		List<Map<String, Object>> children = new ArrayList<Map<String, Object>>();
		for (CategoryTree.Node child : node.getChildren()) {
			children.add(createValue(child));
		}
		parent.put("children", children);
		return parent;
	}
}
