package org.wallride.web.controller.guest.comment;

import org.joda.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.wallride.core.domain.Comment;
import org.wallride.web.support.DomainObjectSavedModel;

import java.io.Serializable;

public class CommentSavedModel extends DomainObjectSavedModel<Long> {

	private String authorName;
	@DateTimeFormat(pattern = "yyyy/MM/dd HH:mm")
	private LocalDateTime date;
	private String content;

	public CommentSavedModel(Comment comment) {
		super(comment);
		authorName = comment.getAuthorName();
		date = comment.getDate();
		content = comment.getContent();
	}

	public String getAuthorName() {
		return authorName;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public String getContent() {
		return content;
	}
}
