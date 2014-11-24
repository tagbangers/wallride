package org.wallride.web.controller.guest.comment;

import org.wallride.core.domain.Comment;

import java.io.Serializable;

public class CommentCreatedModel implements Serializable {

	private String content;

	public CommentCreatedModel(Comment comment) {
		content = comment.getContent();
	}

	public String getContent() {
		return content;
	}
}
