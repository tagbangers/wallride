package org.wallride.core.web.admin.article;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;
import org.wallride.core.domain.Post;
import org.wallride.core.service.ArticleSearchRequest;
import org.wallride.core.web.DomainObjectSearchForm;

@SuppressWarnings("serial")
public class ArticleSearchForm extends DomainObjectSearchForm {
	
	private String keyword;

	private Post.Status status;

	private String language;

	public ArticleSearchForm() {
		this.language = LocaleContextHolder.getLocale().getLanguage();
	}

	public String getKeyword() {
		return keyword;
	}
	
	public void setKeyword(String keyword) {
		this.keyword = keyword;
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

	public ArticleSearchRequest buildArticleSearchRequest() {
		ArticleSearchRequest.Builder builder = new ArticleSearchRequest.Builder();
		return builder
				.keyword(keyword)
				.status(status)
				.language(language)
				.build();
	}
}
