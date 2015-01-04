package org.wallride.web.controller.guest.article;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.LocalDateTime;
import org.springframework.util.StringUtils;
import org.wallride.core.domain.Post;
import org.wallride.core.service.ArticleSearchRequest;
import org.wallride.web.support.DomainObjectSearchForm;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("serial")
public class ArticleSearchForm extends DomainObjectSearchForm {

	private String keyword;
	private LocalDateTime dateFrom;
	private LocalDateTime dateTo;
	private Collection<Long> categoryIds = new ArrayList<>();
	private Collection<String> tagNames = new ArrayList<>();
	private Long authorId;
	private String language;

	public String getKeyword() {
		return keyword;
	}
	
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public LocalDateTime getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(LocalDateTime dateFrom) {
		this.dateFrom = dateFrom;
	}

	public LocalDateTime getDateTo() {
		return dateTo;
	}

	public void setDateTo(LocalDateTime dateTo) {
		this.dateTo = dateTo;
	}

	public Collection<Long> getCategoryIds() {
		return categoryIds;
	}

	public void setCategoryIds(Collection<Long> categoryIds) {
		this.categoryIds = categoryIds;
	}

	public Collection<String> getTagNames() {
		return tagNames;
	}

	public void setTagNames(Collection<String> tagNames) {
		this.tagNames = tagNames;
	}

	public Long getAuthorId() {
		return authorId;
	}

	public void setAuthorId(Long authorId) {
		this.authorId = authorId;
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
		return true;
	}

	public ArticleSearchRequest toArticleSearchRequest() {
		ArticleSearchRequest request = new ArticleSearchRequest();
		request.setKeyword(getKeyword());
		request.setDateFrom(getDateFrom());
		request.setDateTo(getDateTo());
		request.setCategoryIds(getCategoryIds());
		request.setTagNames(getTagNames());
		request.setAuthorId(getAuthorId());
		request.setLanguage(getLanguage());
		request.setStatus(Post.Status.PUBLISHED);
		return request;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (other == null || !(other instanceof ArticleSearchForm)) return false;
		ArticleSearchForm that = (ArticleSearchForm) other;
		return new EqualsBuilder()
				.append(getKeyword(), that.getKeyword())
				.append(getDateFrom(), that.getDateFrom())
				.append(getDateTo(), that.getDateTo())
				.append(getCategoryIds(), that.getCategoryIds())
				.append(getTagNames(), that.getTagNames())
				.append(getAuthorId(), that.getAuthorId())
				.append(getLanguage(), that.getLanguage())
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(getKeyword())
				.append(getDateFrom())
				.append(getDateTo())
				.append(getCategoryIds())
				.append(getTagNames())
				.append(getAuthorId())
				.append(getLanguage())
				.toHashCode();
	}
}
