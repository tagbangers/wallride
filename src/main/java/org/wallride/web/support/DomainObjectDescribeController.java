package org.wallride.web.support;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.wallride.core.domain.DomainObject;

import javax.servlet.http.HttpSession;

public abstract class DomainObjectDescribeController<D extends DomainObject, F extends DomainObjectSearchForm> {

	public String requestMappingDescribe(
			long id,
			Pageable pageable,
			Model model,
			HttpSession session) {
		DomainObject domainObject = readDomainObject(id);
		model.addAttribute(getModelAttributeName(), domainObject);

		D previousObject = null;
		Pageable previousPageable = null;

		D nextObject = null;
		Pageable nextPageable = null;

		DomainObjectSearchCondition<F> condition = DomainObjectSearchCondition.resolve(session, getDomainObjectSearchFormClass());
		if (condition != null) {
			if (pageable != null) {
				condition.setPageable(pageable);
			}

			previousPageable = condition.getPageable();
			nextPageable = condition.getPageable();

			Page<D> searchResults = readDomainObjects(condition.getForm(), condition.getPageable());
			if (searchResults.getContent().contains(domainObject)) {
				D first = searchResults.getContent().get(0);
				D last = searchResults.getContent().get(searchResults.getContent().size() - 1);

				if (ObjectUtils.nullSafeEquals(domainObject, first)) {
					if (searchResults.hasPrevious()) {
						Page<D> previousResults = readDomainObjects(condition.getForm(), condition.getPageable().previousOrFirst());
						previousObject = previousResults.getContent().get(previousResults.getContent().size() - 1);
						previousPageable = condition.getPageable().previousOrFirst();
					}
				} else {
					previousObject = searchResults.getContent().get(searchResults.getContent().indexOf(domainObject) - 1);
				}

				if (ObjectUtils.nullSafeEquals(domainObject, last)) {
					if (searchResults.hasNext()) {
						Page<D> nextResults = readDomainObjects(condition.getForm(), condition.getPageable().next());
						nextObject = nextResults.getContent().get(0);
						nextPageable = condition.getPageable().next();
					}
				} else {
					nextObject = searchResults.getContent().get(searchResults.getContent().lastIndexOf(domainObject) + 1);
				}
			}
		}

		model.addAttribute("previousObject", previousObject);
		model.addAttribute("previousPageable", previousPageable);

		model.addAttribute("nextObject", nextObject);
		model.addAttribute("nextPageable", nextPageable);

		return getViewName();
	}

	protected abstract Class<F> getDomainObjectSearchFormClass();

	protected abstract String getModelAttributeName();
	
	protected abstract String getViewName();

	protected abstract D readDomainObject(long id);

	protected abstract Page<D> readDomainObjects(F form, Pageable pageable);
}
