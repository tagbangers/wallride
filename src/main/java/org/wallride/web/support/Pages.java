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
import org.wallride.core.domain.Page;
import org.wallride.core.model.TreeNode;
import org.wallride.core.support.PageUtils;

import java.util.List;
import java.util.Map;

public class Pages {

	private IProcessingContext processingContext;

	private PageUtils pageUtils;

	public Pages(IProcessingContext processingContext, PageUtils pageUtils) {
		this.processingContext = processingContext;
		this.pageUtils = pageUtils;
	}

	public List<Page> getAllPages() {
		return pageUtils.getAllPages();
	}

	public List<Page> getAllPages(boolean includeUnpublished) {
		return pageUtils.getAllPages(includeUnpublished);
	}

	public List<TreeNode<Page>> getNodes() {
		return pageUtils.getNodes();
	}

	public List<TreeNode<Page>> getNodes(boolean includeUnpublished) {
		return pageUtils.getNodes(includeUnpublished);
	}

	public Map<Page, String> getPaths(Page page) {
		return pageUtils.getPaths(page);
	}

	public List<Page> getChildren(Page page) {
		return pageUtils.getChildren(page);
	}

	public List<Page> getSiblings(Page page) {
		return pageUtils.getSiblings(page);
	}
}