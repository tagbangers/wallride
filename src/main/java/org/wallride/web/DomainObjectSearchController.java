package org.wallride.web;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.domain.DomainObject;
import org.wallride.support.Paginator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Collection;
import java.util.List;

public abstract class DomainObjectSearchController<D extends DomainObject, F extends DomainObjectSearchForm> {
	
	public String requestMappingIndex(
			String token,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response,
			HttpSession session)
			throws Exception {
		DomainObjectSearchCondition<F> condition = DomainObjectSearchCondition.resolve(session, getDomainObjectSearchFormClass(), token);
		if (condition == null) {
			F form = getDomainObjectSearchFormClass().newInstance();
			Paginator<Long> paginator = readDomainObjects(form, 50);
			condition = new DomainObjectSearchCondition<>(session, form, paginator);
		}
		if (!validateCondition(condition, request, response)) {
			return getRedirectViewName();
		}
		List<Long> ids = condition.getPaginator().getAllElements();
		if (ids != null && !ids.isEmpty()) {
			Collection<D> domainObjects = readDomainObjects(condition.getPaginator());
			model.addAttribute(getModelAttributeName(), domainObjects);
		}
		model.addAttribute("form", condition.getForm());
		model.addAttribute("paginator", condition.getPaginator());
		model.addAttribute("token", condition.getToken());

		return getViewName();
	}
	
	public String requestMappingPage(
			int no, 
			String token,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response,
			HttpSession session) {
		DomainObjectSearchCondition<F> condition = DomainObjectSearchCondition.resolve(session, getDomainObjectSearchFormClass(), token);
		if (condition == null) {
			return getRedirectViewName();
		}
		if (!validateCondition(condition, request, response)) {
			return getRedirectViewName();
		}
		if (condition.getPaginator().hasElement()) {
			condition.getPaginator().setNumber(no);
		}
		Collection<D> domainObjects = readDomainObjects(condition.getPaginator());
		model.addAttribute(getModelAttributeName(), domainObjects);
		model.addAttribute("form", condition.getForm());
		model.addAttribute("paginator", condition.getPaginator());
		model.addAttribute("token", condition.getToken());
		
		return getViewName();
	}
	
	public String requestMappingSearch(
			F form,
			BindingResult result,
			Model model,
			HttpSession session,
			RedirectAttributes redirectAttributes) {
		Paginator<Long> paginator = readDomainObjects(form, 50);
		DomainObjectSearchCondition<F> condition = new DomainObjectSearchCondition<>(session, form, paginator);
		
		redirectAttributes.addAttribute("page", paginator.getNumber());
		redirectAttributes.addAttribute("token", condition.getToken());
		return getRedirectViewName();
	}
	
	protected abstract Class<F> getDomainObjectSearchFormClass();
	
	protected abstract String getModelAttributeName();
	
	protected abstract String getViewName();

	protected abstract String getRedirectViewName();

	protected abstract Paginator<Long> readDomainObjects(F form, int perPage);

	protected abstract Collection<D> readDomainObjects(Paginator<Long> paginator);

	protected boolean validateCondition(DomainObjectSearchCondition<F> condition, HttpServletRequest request, HttpServletResponse response) {
		return true;
	}
}
