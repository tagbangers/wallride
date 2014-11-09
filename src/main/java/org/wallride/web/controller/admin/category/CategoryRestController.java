package org.wallride.web.controller.admin.category;

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
import org.wallride.core.domain.Category;
import org.wallride.core.domain.CategoryTree;
import org.wallride.core.service.CategoryService;
import org.wallride.core.support.AuthorizedUser;
import org.wallride.web.support.DomainObjectDeletedModel;
import org.wallride.web.support.DomainObjectSavedModel;
import org.wallride.web.support.DomainObjectUpdatedModel;
import org.wallride.web.support.RestValidationErrorModel;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Controller
public class CategoryRestController {

	@Inject
	private CategoryService categoryService;

	@Inject
	private MessageSourceAccessor messageSourceAccessor;

	private static Logger logger = LoggerFactory.getLogger(CategoryRestController.class);

	@ExceptionHandler(BindException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public @ResponseBody RestValidationErrorModel bindException(BindException e) {
		logger.debug("BindException", e);
		return RestValidationErrorModel.fromBindingResult(e.getBindingResult(), messageSourceAccessor);
	}

	@RequestMapping(value="/categories", method= RequestMethod.GET)
	public @ResponseBody CategoryIndexModel index(@PathVariable String language) {
		CategoryTree categoryTree = categoryService.readCategoryTree(language);
		return new CategoryIndexModel(categoryTree);
	}

	@RequestMapping(value="/categories", method=RequestMethod.POST)
	public @ResponseBody DomainObjectSavedModel save(
			@Valid CategoryCreateForm form,
			BindingResult result,
			AuthorizedUser authorizedUser,
			HttpServletRequest request,
			HttpServletResponse response) throws BindException {
		if (result.hasErrors()) {
			throw new BindException(result);
		}
		Category category = categoryService.createCategory(form.buildCategoryCreateRequest(), authorizedUser);
		FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
		flashMap.put("savedCategory", category);
		RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
		return new DomainObjectSavedModel<Long>(category);
	}

	@RequestMapping(value="/categories/{id}", method=RequestMethod.POST)
	public @ResponseBody DomainObjectUpdatedModel update(
			@Valid CategoryEditForm form,
			BindingResult result,
			@PathVariable long id,
			AuthorizedUser authorizedUser,
			HttpServletRequest request,
			HttpServletResponse response) throws BindException {
		form.setId(id);
		if (result.hasErrors()) {
			throw new BindException(result);
		}
		Category category = categoryService.updateCategory(form.buildCategoryUpdateRequest(), authorizedUser);
		FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
		flashMap.put("updatedCategory", category);
		RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
		return new DomainObjectUpdatedModel<Long>(category);
	}

	@RequestMapping(value="/categories/{id}", method= RequestMethod.DELETE)
	public @ResponseBody DomainObjectDeletedModel<Long> delete(
			@PathVariable String language,
			@PathVariable long id,
			AuthorizedUser authorizedUser,
			HttpServletRequest request,
			HttpServletResponse response) throws BindException {
		Category category = categoryService.deleteCategory(id, language);
		FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
		flashMap.put("deletedCategory", category);
		RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
		return new DomainObjectDeletedModel<Long>(category);
	}

	@RequestMapping(value="/categories", method= RequestMethod.PUT, consumes=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody CategoryIndexModel sort(@PathVariable String language, @RequestBody List<Map<String, Object>> data) {
		categoryService.updateCategoryHierarchy(data, language);
		CategoryTree categoryTree = categoryService.readCategoryTree(language);
		return new CategoryIndexModel(categoryTree);
	}
}
