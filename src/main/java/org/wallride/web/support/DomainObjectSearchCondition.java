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
