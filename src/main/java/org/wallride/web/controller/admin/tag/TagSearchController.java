package org.wallride.web.controller.admin.tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.wallride.core.domain.Category;
import org.wallride.core.domain.CategoryTree;
import org.wallride.core.domain.Tag;
import org.wallride.core.service.TagService;
import org.wallride.core.support.Pagination;

import javax.inject.Inject;

@Controller
@RequestMapping("/{language}/tags/index")
public class TagSearchController {

	@Inject
	private TagService tagService;

	@RequestMapping
	public String index(
			@ModelAttribute("form") TagSearchForm form,
			@PageableDefault(50) Pageable pageable,
			Model model) {
		Page<Tag> tags = tagService.readTags(form.buildTagSearchRequest());
		model.addAttribute("tags", tags);
		model.addAttribute("pageable", pageable);
		model.addAttribute("pagination", new Pagination<>(tags));
		return "/tag/index";
	}

	@RequestMapping(params="part=tag-create-form")
	public String partTagCreateForm(
			@PathVariable String language,
			Model model) {
		return "/tag/index::tag-create-form";
	}

	@RequestMapping(params="part=tag-edit-form")
	public String partTagEditForm(
			@PathVariable String language,
			@RequestParam long id,
			Model model) {
		Tag tag = tagService.readTagById(id, language);
		model.addAttribute("tag", tag);
		return "/tag/index::tag-edit-form";
	}

}