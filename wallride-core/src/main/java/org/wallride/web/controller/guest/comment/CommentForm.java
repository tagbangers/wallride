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

import org.wallride.domain.BlogLanguage;
import org.wallride.domain.User;
import org.wallride.model.CommentCreateRequest;
import org.wallride.model.CommentUpdateRequest;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

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
