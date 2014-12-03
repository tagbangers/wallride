package org.wallride.web.controller.admin.page;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.wallride.core.domain.Post;
import org.wallride.core.service.PageSearchRequest;
import org.wallride.web.support.DomainObjectSearchForm;

@SuppressWarnings("serial")
public class PageSearchForm extends DomainObjectSearchForm {
	
	private String keyword;
	private Long authorId;
	private Post.Status status;
	private String language;

	public PageSearchForm() {
		this.language = LocaleContextHolder.getLocale().getLanguage();
	}

	public String getKeyword() {
		return keyword;
	}
	
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public Long getAuthorId() {
		return authorId;
	}

	public void setAuthorId(Long authorId) {
		this.authorId = authorId;
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

	public boolean isEmpty() {
		if (StringUtils.hasText(getKeyword())) {
			return false;
		}
		if (getStatus() != null) {
			return false;
		}
		if (StringUtils.hasText(getLanguage())) {
			return false;
		}
		return true;
	}
	
	public boolean isAdvanced() {
		return false;
	}

	public PageSearchRequest toPageSearchRequest() {
		return new PageSearchRequest()
				.withKeyword(getKeyword())
				.withAuthorId(getAuthorId())
				.withStatus(getStatus())
				.withLanguage(getLanguage());
	}

	public MultiValueMap<String, String> toQueryParams() {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		if (StringUtils.hasText(keyword)) {
			params.add("keyword", keyword);
		}
		if (authorId != null) {
			params.add("authorId", Long.toString(authorId));
		}
		if (status != null) {
			params.add("status", status.toString());
		}
		return params;
	}
}
