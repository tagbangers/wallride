package org.wallride.core.service;

import org.joda.time.LocalDateTime;
import org.springframework.beans.BeanUtils;
import org.wallride.core.domain.User;
import org.wallride.core.web.DomainObjectEditForm;

import javax.validation.constraints.NotNull;

@SuppressWarnings("serial")
public class UserUpdateRequest extends DomainObjectEditForm {

	@NotNull
	private Long id;

	private LocalDateTime date;
	
	private String title;

	private Long coverId;

	private String body;

	@NotNull
	private String language;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public String getTitle() {
		return title;
	}

	public Long getCoverId() {
		return coverId;
	}

	public void setCoverId(Long coverId) {
		this.coverId = coverId;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public static UserUpdateRequest fromDomainObject(User user) {
		UserUpdateRequest form = new UserUpdateRequest();
		BeanUtils.copyProperties(user, form);
		return form;
	}
}
