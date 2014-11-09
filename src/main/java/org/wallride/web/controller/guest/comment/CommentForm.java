package org.wallride.web.controller.guest.comment;

import org.joda.time.LocalDateTime;
import org.wallride.core.domain.User;
import org.wallride.core.service.CreateCommentRequest;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class CommentForm implements Serializable {

	@NotNull
	private Long postId;
	@NotNull
	private String content;
	@NotNull
	private String language;

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

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public CreateCommentRequest toCreateCommentRequest(User author) {
		CreateCommentRequest request = new CreateCommentRequest();
		request.setPostId(getPostId());
		request.setAuthorId(author.getId());
		request.setDate(LocalDateTime.now());
		request.setContent(getContent());
		request.setApproved(true);
		request.setLanguage(getLanguage());
		return request;
	}
}
