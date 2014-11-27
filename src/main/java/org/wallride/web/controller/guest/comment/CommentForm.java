package org.wallride.web.controller.guest.comment;

import org.joda.time.LocalDateTime;
import org.wallride.core.domain.BlogLanguage;
import org.wallride.core.domain.User;
import org.wallride.core.service.CommentCreateRequest;
import org.wallride.core.service.CommentUpdateRequest;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class CommentForm implements Serializable {

	@NotNull
	private Long postId;
	@NotNull
	private String content;

	public Long getPostId() {
		return postId;
	}

	public void setPostId(Long postId) {
		this.postId = postId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public CommentCreateRequest toCommentCreateRequest(BlogLanguage blogLanguage, User author) {
		CommentCreateRequest request = new CommentCreateRequest();
		request.setBlogLanguage(blogLanguage);
		request.setPostId(getPostId());
		request.setAuthorId(author.getId());
		request.setDate(LocalDateTime.now());
		request.setContent(getContent());
		request.setApproved(true);
		return request;
	}

	public CommentUpdateRequest toCommentUpdateRequest(long id) {
		CommentUpdateRequest request = new CommentUpdateRequest();
		request.setId(id);
		request.setContent(getContent());
		return request;
	}
}
