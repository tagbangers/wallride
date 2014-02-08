package org.wallride.web;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RestValidationErrorModel implements Serializable {

	private List<String> globalErrors;

	private Map<String, String> fieldErrors;

	public List<String> getGlobalErrors() {
		return globalErrors;
	}

	public Map<String, String> getFieldErrors() {
		return fieldErrors;
	}

	public static RestValidationErrorModel fromBindingResult(
			BindingResult result,
			MessageSourceAccessor messageSourceAccessor) {
		RestValidationErrorModel restResult = new RestValidationErrorModel();
		restResult.globalErrors = new ArrayList<>();
		for (ObjectError error : result.getGlobalErrors()) {
			restResult.globalErrors.add(messageSourceAccessor.getMessage(error.getDefaultMessage(), LocaleContextHolder.getLocale()));
		}
		restResult.fieldErrors = new LinkedHashMap<>();
		for (FieldError error : result.getFieldErrors()) {
			restResult.fieldErrors.put(error.getField(), messageSourceAccessor.getMessage(error, LocaleContextHolder.getLocale()));
		}
		return restResult;
	}

}
