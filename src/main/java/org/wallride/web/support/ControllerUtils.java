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

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.Printer;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.datetime.joda.JodaDateTimeFormatAnnotationFormatterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.PropertyDescriptor;
import java.net.URI;
import java.util.Collection;

public abstract class ControllerUtils {

	public static MultiValueMap<String, String> convertBeanForQueryParams(Object target, ConversionService conversionService) {
		BeanWrapperImpl beanWrapper = new BeanWrapperImpl(target);
		beanWrapper.setConversionService(conversionService);
		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap();
		for (PropertyDescriptor pd : beanWrapper.getPropertyDescriptors()) {
			if (beanWrapper.isWritableProperty(pd.getName())) {
				Object pv = beanWrapper.getPropertyValue(pd.getName());
				if (pv != null) {
					if (pv instanceof Collection) {
						if (!CollectionUtils.isEmpty((Collection) pv)) {
							for (Object element : (Collection) pv) {
								queryParams.set(pd.getName(), convertPropertyValueForString(target, pd, element));
							}
						}
					} else {
						queryParams.set(pd.getName(), convertPropertyValueForString(target, pd, pv));
					}
				}
			}
		}
		return queryParams;
	}

	private static String convertPropertyValueForString(Object target, PropertyDescriptor descriptor, Object propertyValue) {
		DateTimeFormat dateTimeFormat;
		try {
			dateTimeFormat = target.getClass().getDeclaredField(descriptor.getName()).getAnnotation(DateTimeFormat.class);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
		if (dateTimeFormat != null) {
			JodaDateTimeFormatAnnotationFormatterFactory factory = new JodaDateTimeFormatAnnotationFormatterFactory();
			Printer printer = factory.getPrinter(dateTimeFormat, descriptor.getPropertyType());
			return printer.print(propertyValue, LocaleContextHolder.getLocale());
		}
		return propertyValue.toString();
	}

	public static ResponseEntity<?> createRedirectResponseEntity(
			HttpServletRequest nativeRequest,
			HttpServletResponse nativeResponse,
			String path) {
		UrlPathHelper pathHelper = new UrlPathHelper();
		String url = pathHelper.getContextPath(nativeRequest) + path;
		String encodedUrl = nativeResponse.encodeRedirectURL(url);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(URI.create(encodedUrl));
		
		ResponseEntity<?> response = new ResponseEntity<>(null, headers, HttpStatus.FOUND);
		return response;
	}
}
