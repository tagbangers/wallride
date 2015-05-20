package org.wallride.web.controller.guest.post;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.wallride.core.domain.Post;
import org.wallride.core.service.PostSearchRequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class PostSearchForm implements Serializable {

	private Collection<String> tagNames = new ArrayList<>();
	private String language;

	public Collection<String> getTagNames() {
		return tagNames;
	}

	public void setTagNames(Collection<String> tagNames) {
		this.tagNames = tagNames;
	}

	public boolean isEmpty() {
//		if (StringUtils.hasText(getKeyword())) {
//			return false;
//		}
		return true;
	}

	public PostSearchRequest toPostSearchRequest(String language) {
		PostSearchRequest request = new PostSearchRequest(language);
		request.setTagNames(getTagNames());
		request.setStatus(Post.Status.PUBLISHED);
		return request;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (other == null || !(other instanceof PostSearchForm)) return false;
		PostSearchForm that = (PostSearchForm) other;
		return new EqualsBuilder()
				.append(getTagNames(), that.getTagNames())
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(getTagNames())
				.toHashCode();
	}
}
