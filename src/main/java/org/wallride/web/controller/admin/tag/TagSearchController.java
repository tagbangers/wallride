package org.wallride.web.controller.admin.tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.Post;
import org.wallride.core.domain.Tag;
import org.wallride.core.service.ArticleService;
import org.wallride.core.service.TagService;
import org.wallride.core.support.Pagination;

import javax.inject.Inject;
import java.util.Map;

@Controller
@RequestMapping("/{language}/tags/index")
@SessionAttributes(types = {TagSearchForm.class})
public class TagSearchController {

	public static final String FORM_MODEL_KEY = "form";

	@Inject
	private TagService tagService;
	@Inject
	private ArticleService articleService;

	@ModelAttribute(FORM_MODEL_KEY)
	public TagSearchForm setupTagSearchForm() {
		return new TagSearchForm();
	}

	@ModelAttribute("articleCounts")
	public Map<Long, Long> articleCounts(@PathVariable String language) {
		return articleService.countArticlesByTagIdGrouped(Post.Status.PUBLISHED, language);
	}

	@RequestMapping
	public String search(
			@PathVariable String language,
			@Validated @ModelAttribute("form") TagSearchForm form,
			BindingResult errors,
			@PageableDefault(50) Pageable pageable,
			Model model) {
		Page<Tag> tags = tagService.readTags(form.buildTagSearchRequest(), pageable);
		model.addAttribute("tags", tags);
		model.addAttribute("pageable", pageable);
		model.addAttribute("pagination", new Pagination<>(tags));
		return "tag/index";
	}

	@RequestMapping(params = "clear")
	public String clear(
			@PathVariable String language,
			SessionStatus sessionStatus,
			RedirectAttributes redirectAttributes) {
		sessionStatus.setComplete();
		return "redirect:/_admin/{language}/tags/index";
	}

	@RequestMapping(params = "part=tag-create-form")
	public String partTagCreateForm(
			@PathVariable String language,
			Model model) {
		return "tag/index::tag-create-form";
	}

	@RequestMapping(params = "part=tag-edit-form")
	public String partTagEditForm(
			@PathVariable String language,
			@RequestParam long id,
			Model model) {
		Tag tag = tagService.readTagById(id, language);
		model.addAttribute("tag", tag);
		return "tag/index::tag-edit-form";
	}

	@RequestMapping(params = "part=bulk-delete-form")
	public String partBulkDeleteForm(
			@PathVariable String language) {
		return "tag/index::bulk-delete-form";
	}
	@RequestMapping(params = "part=merge-tag-form")
	public String mergeTagForm(@PathVariable String language) {
		return "/tag/index::merge-tag-form";
	}
}