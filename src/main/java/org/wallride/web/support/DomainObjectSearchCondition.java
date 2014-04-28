package org.wallride.web.support;

import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpSession;
import java.io.Serializable;

public class DomainObjectSearchCondition<T extends DomainObjectSearchForm> implements Serializable {

	private String sessionKey;
	private T form;
	private Pageable pageable;

	public DomainObjectSearchCondition(HttpSession session, T form) {
		this(session, form, null);
	}
	
	public DomainObjectSearchCondition(HttpSession session, T form, Pageable pageable) {
		this.sessionKey = getSessionKey(form.getClass());
//		this.token = RandomStringUtils.randomAlphanumeric(10);
		this.form = form;
		this.pageable = pageable;
		session.setAttribute(this.sessionKey, this);
	}
	
	public String getSessionKey() {
		return sessionKey;
	}
	
	public T getForm() {
		return form;
	}
	
	public Pageable getPageable() {
		return pageable;
	}

	public void setPageable(Pageable pageable) {
		this.pageable = pageable;
	}

	public static <T extends DomainObjectSearchForm> DomainObjectSearchCondition<T> resolve(HttpSession session, Class<T> clazz) {
		return (DomainObjectSearchCondition<T>) session.getAttribute(getSessionKey(clazz));
	}
	
	private static String getSessionKey(Class<?> clazz) {
		return clazz.getCanonicalName() + ".confition";
	}
}
