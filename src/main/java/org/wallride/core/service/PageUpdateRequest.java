package org.wallride.core.service;

import org.joda.time.LocalDateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.wallride.core.domain.Page;
import org.wallride.core.domain.Post;
import org.wallride.core.web.DomainObjectEditForm;

import javax.validation.constraints.NotNull;

@SuppressWarnings("serial")
public class PageUpdateRequest extends DomainObjectEditForm {
	
	interface GroupPublish {}
	
	@NotNull
	private Long id;

	private String code;

	private String coverId;

	@NotNull(groups=GroupPublish.class)
	private String title;

	@NotNull(groups=GroupPublish.class)
	private String body;
	
	private Long authorId;
	
//	@NotNull
	@DateTimeFormat(pattern="yyyy/MM/dd")
	private LocalDateTime date;

	private Long parentId;
	
	private Post.Status status;
	
	@NotNull
	private String language;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCoverId() {
		return coverId;
	}

	public void setCoverId(String coverId) {
		this.coverId = coverId;
	}

	public String getTitle() {
		return title;
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
	
	public Long getAuthorId() {
		return authorId;
	}
	
	public void setAuthorId(Long authorId) {
		this.authorId = authorId;
	}
	
	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}
	
	public Long getParentId() {
		return parentId;
	}
	
	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
	
	public Post.Status getStatus() {
		return status;
	}
	
	public void setStatus(Post.Status status) {
		this.status = status;
	}
	
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
}
