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
import org.wallride.core.domain.Tag;
import org.wallride.core.service.DuplicateNameException;
import org.wallride.core.service.TagService;
import org.wallride.core.support.AuthorizedUser;
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
	private MessageSourceAccessor messageSourceAccessor;

	private static Logger logger = LoggerFactory.getLogger(TagRestController.class);

	@ExceptionHandler(BindException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public @ResponseBody RestValidationErrorModel bindException(BindException e) {
		logger.debug("BindException", e);
		return RestValidationErrorModel.fromBindingResult(e.getBindingResult(), messageSourceAccessor);
	}

	@RequestMapping(value="/tags", method=RequestMethod.POST)
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

	@RequestMapping(value="/tags/{id}", method=RequestMethod.POST)
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
}
