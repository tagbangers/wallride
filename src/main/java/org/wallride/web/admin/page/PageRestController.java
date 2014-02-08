package org.wallride.web.admin.page;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.wallride.core.domain.Page;
import org.wallride.core.domain.PageTree;
import org.wallride.core.service.PageService;
import org.wallride.core.support.AuthorizedUser;
import org.wallride.web.DomainObjectDeletedModel;
import org.wallride.web.DomainObjectSavedModel;
import org.wallride.web.RestValidationErrorModel;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Controller
public class PageRestController {

	@Inject
	private PageService pageService;

	@Inject
	private MessageSourceAccessor messageSourceAccessor;

	private static Logger logger = LoggerFactory.getLogger(PageRestController.class);

	@ExceptionHandler(BindException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public @ResponseBody RestValidationErrorModel bindException(BindException e) {
		logger.debug("BindException", e);
		return RestValidationErrorModel.fromBindingResult(e.getBindingResult(), messageSourceAccessor);
	}

	@RequestMapping(value="/{language}/pages", method= RequestMethod.GET)
	public @ResponseBody PageIndexModel index(@PathVariable String language) {
		PageTree pageTree = pageService.readPageTree(language);
		return new PageIndexModel(pageTree);
	}

//	@RequestMapping(value="/{language}/pages/{id}", method= RequestMethod.GET)
//	public void describe() {
//
//	}

	@RequestMapping(value="/{language}/pages", method=RequestMethod.POST)
	public @ResponseBody DomainObjectSavedModel save(
			@Valid PageCreateForm form,
			BindingResult result,
			AuthorizedUser authorizedUser,
			HttpServletRequest request,
			HttpServletResponse response) throws BindException {
		if (result.hasErrors()) {
			throw new BindException(result);
		}
		Page page = pageService.createPage(form.buildPageCreateRequest(), result, authorizedUser);
		FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
		flashMap.put("savedPage", page);
		RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
		return new DomainObjectSavedModel<Long>(page);
	}

//	@RequestMapping(value="/{language}/pages/{id}", method=RequestMethod.POST)
//	public @ResponseBody DomainObjectUpdatedModel update(
//			@Valid PageEditForm form,
//			BindingResult result,
//			@PathVariable long id,
//			AuthorizedUser authorizedUser,
//			HttpServletRequest request,
//			HttpServletResponse response) throws BindException {
//		form.setId(id);
//		if (result.hasErrors()) {
//			throw new BindException(result);
//		}
//		Page page = pageService.updatePage(form, result, authorizedUser);
//		FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
//		flashMap.put("updatedPage", page);
//		RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
//		return new DomainObjectUpdatedModel<Long>(page);
//	}

	@RequestMapping(value="/{language}/pages/{id}", method= RequestMethod.DELETE)
	public @ResponseBody DomainObjectDeletedModel<Long> delete(
			@PathVariable String language,
			@PathVariable long id,
			AuthorizedUser authorizedUser,
			HttpServletRequest request,
			HttpServletResponse response) throws BindException {
		Page page = pageService.deletePage(id, language);
		FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
		flashMap.put("deletedPage", page);
		RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
		return new DomainObjectDeletedModel<Long>(page);
	}

	@RequestMapping(value="/{language}/pages", method= RequestMethod.PUT, consumes=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody PageIndexModel sort(@PathVariable String language, @RequestBody List<Map<String, Object>> data) {
		pageService.updatePageHierarchy(data);
		PageTree pageTree = pageService.readPageTree(language);
		return new PageIndexModel(pageTree);
	}
}
