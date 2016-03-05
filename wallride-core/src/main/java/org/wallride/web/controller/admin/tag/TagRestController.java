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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.wallride.domain.Tag;
import org.wallride.exception.DuplicateNameException;
import org.wallride.service.ArticleService;
import org.wallride.service.TagService;
import org.wallride.support.AuthorizedUser;
import org.wallride.web.support.DomainObjectSavedModel;
import org.wallride.web.support.DomainObjectUpdatedModel;
import org.wallride.web.support.RestValidationErrorModel;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
public class TagRestController {

	@Inject
	private TagService tagService;
	@Inject
	private ArticleService articleService;
	@Inject
	private MessageSourceAccessor messageSourceAccessor;

	private static Logger logger = LoggerFactory.getLogger(TagRestController.class);

	@ExceptionHandler(BindException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public @ResponseBody RestValidationErrorModel bindException(BindException e) {
		logger.debug("BindException", e);
		return RestValidationErrorModel.fromBindingResult(e.getBindingResult(), messageSourceAccessor);
	}

	@RequestMapping(value="/{language}/tags", method=RequestMethod.POST)
	public @ResponseBody DomainObjectSavedModel save(
			@Valid TagCreateForm form,
			BindingResult errors,
			AuthorizedUser authorizedUser,
			HttpServletRequest request,
			HttpServletResponse response) throws BindException {
		if (errors.hasErrors()) {
			throw new BindException(errors);
		}

		Tag savedTag;
		try {
			savedTag = tagService.createTag(form.buildTagCreateRequest(), authorizedUser);
		} catch (DuplicateNameException e) {
			errors.rejectValue("name", "NotDuplicate");
			throw new BindException(errors);
		}

		FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
		flashMap.put("savedTag", savedTag);
		RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
		return new DomainObjectSavedModel<>(savedTag);
	}

	@RequestMapping(value="/{language}/tags/{id}", method=RequestMethod.POST)
	public @ResponseBody DomainObjectUpdatedModel update(
			@Valid TagEditForm form,
			BindingResult errors,
			@PathVariable long id,
			AuthorizedUser authorizedUser,
			HttpServletRequest request,
			HttpServletResponse response) throws BindException {
		form.setId(id);
		if (errors.hasErrors()) {
			throw new BindException(errors);
		}

		Tag savedTag;
		try {
			savedTag = tagService.updateTag(form.buildTagUpdateRequest(), authorizedUser);
		} catch (DuplicateNameException e) {
			errors.rejectValue("name", "NotDuplicate");
			throw new BindException(errors);
		}

		FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
		flashMap.put("savedTag", savedTag);
		RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
		return new DomainObjectUpdatedModel<>(savedTag);
	}

	@RequestMapping(value = "/{language}/tags/merge", method = RequestMethod.POST)
	public @ResponseBody DomainObjectSavedModel merge(
			@Valid TagMergeForm form,
			BindingResult errors,
			AuthorizedUser authorizedUser,
			HttpServletRequest request,
			HttpServletResponse response) throws BindException {
		if (errors.hasErrors()) {
			throw new BindException(errors);
		}

		Tag mergedTag;
		try {
			mergedTag = tagService.mergeTags(form.toTagMergeRequest(), authorizedUser);
		} catch (DuplicateNameException e) {
			errors.rejectValue("name", "NotDuplicate");
			throw new BindException(errors);
		}

		FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
		flashMap.put("mergedTag", mergedTag);
		RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
		return new DomainObjectSavedModel<>(mergedTag);
	}
}
