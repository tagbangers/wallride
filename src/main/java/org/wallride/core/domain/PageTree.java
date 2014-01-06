package org.wallride.core.domain;

import java.io.Serializable;
import java.util.*;

public class PageTree implements Serializable {

	private Map<Long, Page> pageMap = new LinkedHashMap<>();

	private List<Node> rootNodes = new ArrayList<>();

	public PageTree(Collection<Page> pages) {
		pages = new TreeSet<>(pages);
		for (Page page : pages) {
			pageMap.put(page.getId(), page);
		}

		Iterator<Page> i = pages.iterator();
		while (i.hasNext()) {
			Page page = i.next();
			if (page.getParent() == null) {
				rootNodes.add(new Node(page));
				i.remove();
			}
		}

		for (Node node : rootNodes) {
			createChildren(node, pages);
		}
	}

	private void createChildren(Node parent, Collection<Page> pages) {
		List<Node> children = new ArrayList<>();
		Iterator<Page> i = pages.iterator();
		while (i.hasNext()) {
			Page page = i.next();
			Node node = new Node(page);
			if (parent.getPage().equals(page.getParent())) {
				children.add(node);
				i.remove();
//				createChildren(node, categories);
			}
		}
		parent.children = children;

		for (Node node : children) {
			createChildren(node, pages);
		}
	}

	public List<Node> getRootNodes() {
		return rootNodes;
	}

	public Collection<Page> getPages() {
		return pageMap.values();
	}

	public Page getPage(long id) {
		return pageMap.get(id);
	}

	public boolean isEmpty() {
		return pageMap.isEmpty();
	}

	public class Node {

		private Page page;

		private List<Node> children = new ArrayList<>();

		private Node(Page page) {
			this.page = page;
		}

		public Page getPage() {
			return page;
		}

		public List<Node> getChildren() {
			return children;
		}
	}
}
