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

package org.wallride.model;

import org.wallride.domain.Page;

import java.util.ArrayList;
import java.util.List;

public class TreeNode<T> {

	private T object;

	private TreeNode parent;

	private List<TreeNode<T>> children = new ArrayList<>();

	public TreeNode(T object) {
		this.object = object;
	}

	public T getObject() {
		return object;
	}

	public TreeNode getParent() {
		return parent;
	}

	public void setParent(TreeNode parent) {
		this.parent = parent;
	}

	public List<TreeNode<T>> getChildren() {
		return children;
	}

	public void setChildren(List<TreeNode<T>> children) {
		this.children = children;
	}

	public boolean contains(Page page) {
		if (getObject().equals(page)) {
			return true;
		}
		for (TreeNode node : getChildren()) {
			if (node.contains(page)) {
				return true;
			}
		}
		return false;
	}
}
