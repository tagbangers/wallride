package org.wallride.web.admin.page;

import org.wallride.core.domain.PageTree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PageIndexModel extends ArrayList<Map<String, Object>> {

	public PageIndexModel(PageTree pageTree) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

		for (PageTree.Node node : pageTree.getRootNodes()) {
			result.add(createValue(node));
		}
		this.addAll(result);
	}

	private Map<String, Object> createValue(PageTree.Node node) {
		Map<String, Object> parent = new LinkedHashMap<>();
		parent.put("id", node.getPage().getId());
		parent.put("code", node.getPage().getCode());
		parent.put("title", node.getPage().getTitle());
//		parent.put("articleCount", page.getArticleCount());

		List<Map<String, Object>> children = new ArrayList<Map<String, Object>>();
		for (PageTree.Node child : node.getChildren()) {
			children.add(createValue(child));
		}
		parent.put("children", children);
		return parent;
	}
}
