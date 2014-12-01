package org.wallride.web.controller.admin.page;

import org.joda.time.LocalDateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.wallride.core.domain.Page;
import org.wallride.core.domain.Post;
import org.wallride.core.service.PageUpdateRequest;
import org.wallride.web.support.DomainObjectEditForm;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public class PageEditForm extends DomainObjectEditForm {
	
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
	private Set<Long> relatedPostIds = new HashSet<>();

	private String seoTitle;
	private String seoDescription;
	private String seoKeywords;

//	private Post.Status status;
	
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

	public Set<Long> getRelatedPostIds() {
		return relatedPostIds;
	}

	public void setRelatedPostIds(Set<Long> relatedPostIds) {
		this.relatedPostIds = relatedPostIds;
	}

	public String getSeoTitle() {
		return seoTitle;
	}

	public void setSeoTitle(String seoTitle) {
		this.seoTitle = seoTitle;
	}

	public String getSeoDescription() {
		return seoDescription;
	}

	public void setSeoDescription(String seoDescription) {
		this.seoDescription = seoDescription;
	}

	public String getSeoKeywords() {
		return seoKeywords;
	}

	public void setSeoKeywords(String seoKeywords) {
		this.seoKeywords = seoKeywords;
	}

	//	public Post.Status getStatus() {
//		return status;
//	}
//
//	public void setStatus(Post.Status status) {
//		this.status = status;
//	}
	
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public PageUpdateRequest buildPageUpdateRequest() {
		PageUpdateRequest.Builder builder = new PageUpdateRequest.Builder();
		return builder
				.id(id)
				.code(code)
				.coverId(coverId)
				.title(title)
				.body(body)
				.authorId(authorId)
				.date(date)
				.parentId(parentId)
				.relatedPostIds(relatedPostIds)
				.seoTitle(seoTitle)
				.seoDescription(seoDescription)
				.seoKeywords(seoKeywords)
//				.status(status)
				.language(language)
				.build();
	}

	public static PageEditForm fromDomainObject(Page page) {
		PageEditForm form = new PageEditForm();
		BeanUtils.copyProperties(page, form);

		if (page.getStatus().equals(Post.Status.DRAFT)) {
			form.setCode(page.getDraftedCode());
		}

		form.setCoverId(page.getCover() != null ? page.getCover().getId() : null);
		form.setParentId(page.getParent() != null ? page.getParent().getId() : null);

		for (Post post : page.getRelatedToPosts()) {
			form.getRelatedPostIds().add(post.getId());
		}

		if (page.getSeo() != null) {
			form.setSeoTitle(page.getSeo().getTitle());
			form.setSeoDescription(page.getSeo().getDescription());
			form.setSeoKeywords(page.getSeo().getKeywords());
		}
		return form;
	}
}
