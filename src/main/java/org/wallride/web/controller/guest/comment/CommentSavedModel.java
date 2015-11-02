/*
 * Copyright 2014 Tagbangers, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wallride.web.controller.guest.comment;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.LocalDateTime;
import org.wallride.core.domain.Comment;
import org.wallride.core.support.LocalDateTimeSerializer;
import org.wallride.web.support.DomainObjectSavedModel;

public class CommentSavedModel extends DomainObjectSavedModel<Long> {

	private String authorName;
	@JsonSerialize(using = LocalDateTimeSerializer.class)
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
