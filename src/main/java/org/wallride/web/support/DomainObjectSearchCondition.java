package org.wallride.web.support;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.util.StringUtils;
import org.wallride.core.support.Paginator;

import javax.servlet.http.HttpSession;

public class DomainObjectSearchCondition<T extends DomainObjectSearchForm> {

	private String sessionKey;
	
	private String token;
	
	private T form;
	
	private Paginator<Long> paginator;
	
	public DomainObjectSearchCondition(HttpSession session, T form) {
		this(session, form, null);
	}
	
	public DomainObjectSearchCondition(HttpSession session, T form, Paginator<Long> paginator) {
		this.sessionKey = getSessionKey(form.getClass());
		this.token = RandomStringUtils.randomAlphanumeric(10);
		this.form = form;
		this.paginator = paginator;
		session.setAttribute(this.sessionKey, this);
	}
	
	public String getSessionKey() {
		return sessionKey;
	}
	
	public String getToken() {
		return token;
	}
	
	public T getForm() {
		return form;
	}
	
	public Paginator<Long> getPaginator() {
		return paginator;
	}
	
	public static <T extends DomainObjectSearchForm> DomainObjectSearchCondition<T> resolve(HttpSession session, Class<T> clazz, String token) {
		@SuppressWarnings("unchecked")
		DomainObjectSearchCondition<T> condition = (DomainObjectSearchCondition<T>) session.getAttribute(getSessionKey(clazz));
		if (condition == null || !StringUtils.hasText(token)) {
			return null;
		}
		if (!token.equals(condition.getToken())) {
			return null;
		}
		return condition;
	}
	
	private static String getSessionKey(Class<?> clazz) {
		return clazz.getCanonicalName() + ".confition";
	}
}
