package org.wallride.web.controller.admin.customfield;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.wallride.service.CustomFieldService;
import org.wallride.web.support.RestValidationErrorModel;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class CustomFieldRestController {

	@Inject
	private CustomFieldService customFieldService;

	@Inject
	private MessageSourceAccessor messageSourceAccessor;

	private static Logger logger = LoggerFactory.getLogger(CustomFieldRestController.class);

	@ExceptionHandler(BindException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public @ResponseBody
	RestValidationErrorModel bindException(BindException e) {
		logger.debug("BindException", e);
		return RestValidationErrorModel.fromBindingResult(e.getBindingResult(), messageSourceAccessor);
	}

	@RequestMapping(value="/{language}/customfields", method= RequestMethod.PUT, consumes= MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	CustomFieldIndexModel sort(@PathVariable String language, @RequestBody List<String> ids, BindingResult result) {
		List<Long> idList = ids.stream().map(Long::valueOf).collect(Collectors.toList());
		customFieldService.updateCustomFieldOrder(idList, language, result);
		return new CustomFieldIndexModel(idList, result);
	}
}
