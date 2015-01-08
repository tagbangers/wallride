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

package org.wallride.core.service;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.wallride.core.domain.Comment;
import org.wallride.core.domain.Post;
import org.wallride.core.domain.User;
import org.wallride.core.repository.CommentRepository;
import org.wallride.core.repository.PostRepository;
import org.wallride.core.repository.UserRepository;
import org.wallride.core.support.AuthorizedUser;

import javax.annotation.Resource;

@Service
@Transactional(rollbackFor = Exception.class)
public class CommentService {

	@Resource
	private CommentRepository commentRepository;
	@Resource
	private PostRepository postRepository;
	@Resource
	private UserRepository userRepository;

	private static Logger logger = LoggerFactory.getLogger(CommentService.class);

	public Comment createComment(CommentCreateRequest request, AuthorizedUser createdBy) {
		Post post = postRepository.findById(request.getPostId(), request.getBlogLanguage().getLanguage());
		if (post == null) {
			throw new ServiceException("Post was not found [" + request.getPostId() + "]");
		}

		User author = userRepository.findById(request.getAuthorId());

		LocalDateTime now = LocalDateTime.now();
		Comment comment = new Comment();
		comment.setPost(post);
		comment.setAuthor(author);
		comment.setAuthorName(author.toString());
		comment.setDate(request.getDate());
		comment.setContent(request.getContent());
		comment.setApproved(request.isApproved());

		comment.setCreatedAt(now);
		comment.setCreatedBy(createdBy.toString());
		comment.setUpdatedAt(now);
		comment.setUpdatedBy(createdBy.toString());

		return commentRepository.saveAndFlush(comment);
	}

	public Comment updateComment(CommentUpdateRequest request, AuthorizedUser updatedBy) {
		Comment comment = commentRepository.findByIdForUpdate(request.getId());
		if (comment == null) {
			throw new ServiceException();
		}
		if (!ObjectUtils.nullSafeEquals(comment.getAuthor(), updatedBy)) {
			throw new ServiceException();
		}

		LocalDateTime now = LocalDateTime.now();
		comment.setContent(request.getContent());
		comment.setUpdatedAt(now);
		comment.setUpdatedBy(updatedBy.toString());
		return commentRepository.saveAndFlush(comment);
	}

	public Comment deleteComment(CommentDeleteRequest request, AuthorizedUser deletedBy) {
		Comment comment = commentRepository.findByIdForUpdate(request.getId());
		if (comment == null) {
			throw new ServiceException();
		}
		if (!ObjectUtils.nullSafeEquals(comment.getAuthor(), deletedBy)) {
			throw new ServiceException();
		}
		commentRepository.delete(comment);
		return comment;
	}

	public Page<Comment> readComments(CommentSearchRequest request) {
		Pageable pageable = new PageRequest(0, 10);
		return readComments(request, pageable);
	}

	public Page<Comment> readComments(CommentSearchRequest request, Pageable pageable) {
		return commentRepository.search(request, pageable);
	}
}
