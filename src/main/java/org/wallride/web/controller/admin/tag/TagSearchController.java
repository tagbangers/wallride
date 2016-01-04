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

package org.wallride.web.controller.admin.tag;

import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.wallride.core.domain.Post;
import org.wallride.core.domain.Tag;
import org.wallride.core.service.ArticleService;
import org.wallride.core.service.TagService;
import org.wallride.web.support.ControllerUtils;
import org.wallride.web.support.Pagination;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

@Controller
@RequestMapping("/{language}/tags/index")
public class TagSearchController {

	public static final String FORM_MODEL_KEY = "form";

	@Inject
	private TagService tagService;

	@Inject
	private ArticleService articleService;

	@Inject
	private ConversionService conversionService;

	@ModelAttribute(FORM_MODEL_KEY)
	public TagSearchForm setupTagSearchForm() {
		return new TagSearchForm();
	}

	@ModelAttribute("articleCounts")
	public Map<Long, Long> articleCounts(@PathVariable String language) {
		return articleService.countArticlesByTagIdGrouped(Post.Status.PUBLISHED, language);
	}

	@ModelAttribute("query")
	public String query(@RequestParam(required = false) String query) {
		return query;
	}

	@RequestMapping
	public String search(
			@PathVariable String language,
			@Validated @ModelAttribute("form") TagSearchForm form,
			BindingResult errors,
			@PageableDefault(50) Pageable pageable,
			Model model,
			HttpServletRequest servletRequest) throws UnsupportedEncodingException {
		Page<Tag> tags = tagService.getTags(form.toTagSearchRequest(), pageable);

		model.addAttribute("tags", tags);
		model.addAttribute("pageable", pageable);
		model.addAttribute("pagination", new Pagination<>(tags, servletRequest));

		UriComponents uriComponents = ServletUriComponentsBuilder
				.fromRequest(servletRequest)
				.queryParams(ControllerUtils.convertBeanForQueryParams(form, conversionService))
				.build();
		if (!StringUtils.isEmpty(uriComponents.getQuery())) {
			model.addAttribute("query", URLDecoder.decode(uriComponents.getQuery(), "UTF-8"));
		}

		return "tag/index";
	}

	@RequestMapping(params = "query")
	public String search(
			@PathVariable String language,
			String query,
			Model model,
			SessionStatus sessionStatus,
			RedirectAttributes redirectAttributes) {
		sessionStatus.setComplete();

		for (Map.Entry<String, Object> mapEntry : model.asMap().entrySet()) {
			redirectAttributes.addFlashAttribute(mapEntry.getKey(), mapEntry.getValue());
		}
		String url = UriComponentsBuilder.fromPath("/_admin/{language}/tags/index")
				.query(query)
				.buildAndExpand(language)
				.encode()
				.toUriString();
		return "redirect:" + url;
	}

	@RequestMapping(method = RequestMethod.GET, params = "part=tag-create-form")
	public String partTagCreateForm(
			@PathVariable String language,
			Model model) {
		return "tag/index::tag-create-form";
	}

	@RequestMapping(method = RequestMethod.GET, params = "part=tag-edit-form")
	public String partTagEditForm(
			@PathVariable String language,
			@RequestParam long id,
			Model model) {
		Tag tag = tagService.getTagById(id, language);
		model.addAttribute("tag", tag);
		return "tag/index::tag-edit-form";
	}

	@RequestMapping(method = RequestMethod.GET, params = "part=tag-merge-form")
	public String mergeTagForm(@PathVariable String language) {
		return "tag/index::tag-merge-form";
	}

	@RequestMapping(method = RequestMethod.GET, params = "part=bulk-delete-form")
	public String partBulkDeleteForm(@PathVariable String language) {
		return "tag/index::bulk-delete-form";
	}
}