package org.wallride.web.support;

import org.springframework.ui.Model;
import org.wallride.core.domain.DomainObject;

import javax.servlet.http.HttpSession;
import java.util.List;

public abstract class DomainObjectDescribeController<D extends DomainObject, F extends DomainObjectSearchForm> {

	public String requestMappingDescribe(
			long id, 
			String token,
			Model model,
			HttpSession session) {
		DomainObject domainObject = readDomainObject(id);
		model.addAttribute(getModelAttributeName(), domainObject);
		
		DomainObjectSearchCondition<F> condition = DomainObjectSearchCondition.resolve(session, getDomainObjectSearchFormClass(), token);
		if (condition != null) {
			List<Long> searchResults = condition.getPaginator().getAllElements();
			int index = searchResults.indexOf(id);
			if (index > 0) {
				model.addAttribute("prevId", searchResults.get(index - 1));
			}
			if (index < searchResults.size() - 1) {
				model.addAttribute("nextId", searchResults.get(index + 1));
			}
			model.addAttribute("token", condition.getToken());
		}
		
		return getViewName();
	}

	protected abstract Class<F> getDomainObjectSearchFormClass();

	protected abstract String getModelAttributeName();
	
	protected abstract String getViewName();

	protected abstract D readDomainObject(long id);
}
