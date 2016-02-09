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

package org.wallride.web.support;

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
