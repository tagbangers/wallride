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

package org.wallride.web.controller.admin.page;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.wallride.core.domain.Page;
import org.wallride.core.service.PageService;
import org.wallride.web.support.HttpNotFoundException;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.List;

@Controller
@RequestMapping(value="/{language}/pages/describe", method=RequestMethod.GET)
public class PageDescribeController {

	@Inject
	private PageService pageService;

	@Inject
	private ConversionService conversionService;

	@ModelAttribute("query")
	public String query(@RequestParam(required = false) String query) {
		return query;
	}

	@RequestMapping
	public String describe(
			@PathVariable String language,
			@RequestParam long id,
			String query,
			Model model) {
		Page page = pageService.readPageById(id, language);
		if (page == null) {
			throw new HttpNotFoundException();
		}

		MutablePropertyValues mpvs = new MutablePropertyValues(UriComponentsBuilder.newInstance().query(query).build().getQueryParams());
		for (Iterator<PropertyValue> i = mpvs.getPropertyValueList().iterator(); i.hasNext();) {
			PropertyValue pv = i.next();
			boolean hasValue = false;
			for (String value : (List<String>) pv.getValue()) {
				if (StringUtils.hasText(value)) {
					hasValue = true;
					break;
				}
			}
			if (!hasValue) {
				i.remove();
			}
		}
		BeanWrapperImpl beanWrapper = new BeanWrapperImpl(new PageSearchForm());
		beanWrapper.setConversionService(conversionService);
		beanWrapper.setPropertyValues(mpvs, true, true);
		PageSearchForm form = (PageSearchForm) beanWrapper.getWrappedInstance();
		List<Long> ids = pageService.readPageIds(form.toPageSearchRequest());
		if (!CollectionUtils.isEmpty(ids)) {
			int index = ids.indexOf(page.getId());
			if (index < ids.size() - 1) {
				Long next = ids.get(index + 1);
				model.addAttribute("next", next);
			}
			if (index > 0) {
				Long prev = ids.get(index -1);
				model.addAttribute("prev", prev);
			}
		}

		model.addAttribute("page", page);
		model.addAttribute("query", query);
		return "page/describe";
	}

	@RequestMapping(params="part=delete-form")
	public String partDeleteDialog(
			@PathVariable String language,
			@RequestParam long id, Model model) {
		Page page = pageService.readPageById(id, language);
		model.addAttribute("page", page);
		return "page/describe::delete-form";
	}
}