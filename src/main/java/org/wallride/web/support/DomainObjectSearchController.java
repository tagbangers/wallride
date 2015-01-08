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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.DomainObject;
import org.wallride.core.support.Pagination;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public abstract class DomainObjectSearchController<D extends DomainObject, F extends DomainObjectSearchForm> {
	
	public String requestMappingIndex(
			Model model,
			HttpServletRequest request,
			HttpServletResponse response,
			HttpSession session)
			throws Exception {
		DomainObjectSearchCondition<F> condition = DomainObjectSearchCondition.resolve(session, getDomainObjectSearchFormClass());
		if (condition == null) {
			F form = getDomainObjectSearchFormClass().newInstance();
			Pageable pageable = new PageRequest(0, 50);
			condition = new DomainObjectSearchCondition<>(session, form, pageable);
		}
		if (!validateCondition(condition, request, response)) {
			F form = getDomainObjectSearchFormClass().newInstance();
			Pageable pageable = new PageRequest(0, 50);
			condition = new DomainObjectSearchCondition<>(session, form, pageable);
		}
		Page<D> domainObjects = readDomainObjects(condition.getForm(), condition.getPageable());
		model.addAttribute(getModelAttributeName(), domainObjects);

		model.addAttribute("form", condition.getForm());
		model.addAttribute("pageable", condition.getPageable());
		model.addAttribute("pagination", new Pagination<>(domainObjects));

		return getViewName();
	}
	
	public String requestMappingPage(
			Pageable pageable,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response,
			HttpSession session) {
		DomainObjectSearchCondition<F> condition = DomainObjectSearchCondition.resolve(session, getDomainObjectSearchFormClass());
		if (condition == null) {
			return getRedirectViewName();
		}
		if (!validateCondition(condition, request, response)) {
			return getRedirectViewName();
		}
		condition.setPageable(pageable);

		Page<D> domainObjects = readDomainObjects(condition.getForm(), condition.getPageable());
		model.addAttribute(getModelAttributeName(), domainObjects);
		model.addAttribute("form", condition.getForm());
		model.addAttribute("pageable", condition.getPageable());
		model.addAttribute("pagination", new Pagination<>(domainObjects));

		return getViewName();
	}
	
	public String requestMappingSearch(
			F form,
			BindingResult result,
			Model model,
			HttpSession session,
			RedirectAttributes redirectAttributes) {
		Pageable pageable = new PageRequest(0, 50);
		DomainObjectSearchCondition<F> condition = new DomainObjectSearchCondition<>(session, form, pageable);
		
		return getRedirectViewName();
	}

	protected abstract Class<F> getDomainObjectSearchFormClass();
	
	protected abstract String getModelAttributeName();
	
	protected abstract String getViewName();

	protected abstract String getRedirectViewName();

	protected abstract Page<D> readDomainObjects(F form, Pageable pageable);

	protected boolean validateCondition(DomainObjectSearchCondition<F> condition, HttpServletRequest request, HttpServletResponse response) {
		return true;
	}
}
