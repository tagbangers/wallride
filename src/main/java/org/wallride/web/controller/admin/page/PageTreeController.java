package org.wallride.web.controller.admin.page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.wallride.core.domain.Page;
import org.wallride.core.domain.PageTree;
import org.wallride.core.service.PageService;

import javax.inject.Inject;

@Controller
@RequestMapping("/{language}/pages/tree")
public class PageTreeController {

	@Inject
	private PageService pageService;

	@ModelAttribute("form")
	public PageCreateForm pageCreateForm() {
		return new PageCreateForm();
	}

	@RequestMapping
	public String index(@PathVariable String language, Model model) {
		PageTree pageTree = pageService.readPageTree(language);
		model.addAttribute("pageTree", pageTree);
		return "page/tree";
	}

	@RequestMapping(params="part=page-create-form")
	public String partPageCreateDialog(@PathVariable String language, @RequestParam(required=false) Long parentId, Model model) {
		PageTree pageTree = pageService.readPageTree(language);
		model.addAttribute("parentId", parentId);
		model.addAttribute("pageTree", pageTree);
		return "page/tree::page-create-form";
	}

	@RequestMapping(params="part=page-edit-form")
	public String partPageEditDialog(@PathVariable String language, @RequestParam long id, Model model) {
		PageTree pageTree = pageService.readPageTree(language);
		Page page = pageTree.getPageById(id);
		model.addAttribute("pageTree", pageTree);
		model.addAttribute("page", page);
		return "page/tree::page-edit-form";
	}

	@RequestMapping(params="part=page-delete-form")
	public String partPageDeleteDialog(@RequestParam long id, Model model) {
		model.addAttribute("targetId", id);
		return "page/tree::page-delete-form";
	}
}