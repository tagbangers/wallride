package org.wallride.web.controller.admin.category;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.wallride.core.domain.Category;
import org.wallride.core.domain.CategoryTree;
import org.wallride.core.service.CategoryService;

import javax.inject.Inject;

@Controller
@RequestMapping("/{language}/categories/index")
public class CategoryIndexController {

	@Inject
	private CategoryService categoryService;

	@ModelAttribute("form")
	public CategoryCreateForm categoryCreateForm() {
		return new CategoryCreateForm();
	}

	@RequestMapping
	public String index(@PathVariable String language, Model model) {
		CategoryTree categoryTree = categoryService.readCategoryTree(language);
		model.addAttribute("categoryTree", categoryTree);
		return "/category/index";
	}

	@RequestMapping(params="part=category-create-dialog")
	public String partCategoryCreateDialog(@PathVariable String language, @RequestParam(required=false) Long parentId, Model model) {
		CategoryTree categoryTree = categoryService.readCategoryTree(language);
		model.addAttribute("parentId", parentId);
		model.addAttribute("categoryTree", categoryTree);
		return "/category/index::#category-create-dialog";
	}

	@RequestMapping(params="part=category-edit-dialog")
	public String partCategoryEditDialog(@PathVariable String language, @RequestParam long id, Model model) {
		CategoryTree categoryTree = categoryService.readCategoryTree(language);
		Category category = categoryTree.getCategoryById(id);
		model.addAttribute("categoryTree", categoryTree);
		model.addAttribute("category", category);
		return "/category/index::#category-edit-dialog";
	}

	@RequestMapping(params="part=category-delete-dialog")
	public String partCategoryDeleteDialog(@RequestParam long id, Model model) {
		model.addAttribute("targetId", id);
		return "/category/index::#category-delete-dialog";
	}
}