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

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.wallride.core.domain.Category;
import org.wallride.core.domain.CategoryTree;
import org.wallride.core.domain.Post;
import org.wallride.core.service.ArticleService;
import org.wallride.core.service.CategoryService;

import javax.inject.Inject;
import java.util.Map;

@Controller
@RequestMapping("/{language}/categories/index")
public class CategoryIndexController {

	@Inject
	private CategoryService categoryService;
	@Inject
	private ArticleService articleService;

	@ModelAttribute("form")
	public CategoryCreateForm categoryCreateForm() {
		return new CategoryCreateForm();
	}

	@ModelAttribute("articleCounts")
	public Map<Long, Long> articleCounts(@PathVariable String language) {
		return articleService.countArticlesByCategoryIdGrouped(Post.Status.PUBLISHED, language);
	}

	@RequestMapping
	public String index(@PathVariable String language, Model model) {
		CategoryTree categoryTree = categoryService.getCategoryTree(language);
		model.addAttribute("categoryTree", categoryTree);
		return "category/index";
	}

	@RequestMapping(params="part=category-create-form")
	public String partCategoryCreateForm(@PathVariable String language, @RequestParam(required = false) Long parentId, Model model) {
		CategoryTree categoryTree = categoryService.getCategoryTree(language);
		model.addAttribute("parentId", parentId);
		model.addAttribute("categoryTree", categoryTree);
		return "category/index::category-create-form";
	}

	@RequestMapping(params="part=category-edit-form")
	public String partCategoryEditForm(@PathVariable String language, @RequestParam long id, Model model) {
		CategoryTree categoryTree = categoryService.getCategoryTree(language);
		Category category = categoryTree.getCategoryById(id);
		model.addAttribute("categoryTree", categoryTree);
		model.addAttribute("category", category);
		return "category/index::category-edit-form";
	}

	@RequestMapping(params="part=category-delete-form")
	public String partCategoryDeleteForm(@RequestParam long id, Model model) {
		model.addAttribute("targetId", id);
		return "category/index::category-delete-form";
	}
}