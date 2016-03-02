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

package org.wallride.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.wallride.domain.Comment;
import org.wallride.domain.Post;
import org.wallride.domain.User;
import org.wallride.exception.ServiceException;
import org.wallride.model.*;
import org.wallride.repository.CommentRepository;
import org.wallride.repository.PostRepository;
import org.wallride.repository.UserRepository;
import org.wallride.support.AuthorizedUser;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
		Post post = postRepository.findOneByIdAndLanguage(request.getPostId(), request.getBlogLanguage().getLanguage());
		if (post == null) {
			throw new ServiceException("Post was not found [" + request.getPostId() + "]");
		}

		User author = userRepository.findOneById(request.getAuthorId());

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
		Comment comment = commentRepository.findOneForUpdateById(request.getId());
		if (comment == null) {
			throw new ServiceException();
		}
		if (!updatedBy.getRoles().contains(User.Role.ADMIN) || !ObjectUtils.nullSafeEquals(comment.getAuthor(), updatedBy)) {
			throw new ServiceException();
		}

		LocalDateTime now = LocalDateTime.now();
		comment.setContent(request.getContent());
		comment.setUpdatedAt(now);
		comment.setUpdatedBy(updatedBy.toString());
		return commentRepository.saveAndFlush(comment);
	}

	public Comment deleteComment(CommentDeleteRequest request, AuthorizedUser deletedBy) {
		Comment comment = commentRepository.findOneForUpdateById(request.getId());
		if (comment == null) {
			throw new ServiceException();
		}
		if (!deletedBy.getRoles().contains(User.Role.ADMIN) || !ObjectUtils.nullSafeEquals(comment.getAuthor(), deletedBy)) {
			throw new ServiceException();
		}
		commentRepository.delete(comment);
		return comment;
	}

	public List<Comment> bulkApproveComment(CommentBulkApproveRequest request, AuthorizedUser authorizedUser) {
		if (!authorizedUser.getRoles().contains(User.Role.ADMIN)) {
			throw new ServiceException();
		}

		List<Comment> comments = new ArrayList<>();
		for (long id : request.getIds()) {
			Comment comment = commentRepository.findOneForUpdateById(id);
			if (comment.isApproved()) {
				continue;
			}

			LocalDateTime now = LocalDateTime.now();
			comment.setApproved(true);
			comment.setUpdatedAt(now);
			comment.setUpdatedBy(authorizedUser.toString());
			comment = commentRepository.saveAndFlush(comment);

			comments.add(comment);
		}
		return comments;
	}

	public List<Comment> bulkUnapproveComment(CommentBulkUnapproveRequest request, AuthorizedUser authorizedUser) {
		if (!authorizedUser.getRoles().contains(User.Role.ADMIN)) {
			throw new ServiceException();
		}

		List<Comment> comments = new ArrayList<>();
		for (long id : request.getIds()) {
			Comment comment = commentRepository.findOneForUpdateById(id);
			if (!comment.isApproved()) {
				continue;
			}

			LocalDateTime now = LocalDateTime.now();
			comment.setApproved(false);
			comment.setUpdatedAt(now);
			comment.setUpdatedBy(authorizedUser.toString());
			comment = commentRepository.saveAndFlush(comment);

			comments.add(comment);
		}
		return comments;
	}

	public List<Comment> bulkDeleteComment(CommentBulkDeleteRequest bulkDeleteRequest, AuthorizedUser deletedBy) {
		List<Comment> comments = new ArrayList<>();
		for (long id : bulkDeleteRequest.getIds()) {
			CommentDeleteRequest request = new CommentDeleteRequest();
			request.setId(id);
			Comment comment = deleteComment(request, deletedBy);
			comments.add(comment);
		}
		return comments;
	}

	public Page<Comment> getComments(CommentSearchRequest request) {
		Pageable pageable = new PageRequest(0, 10);
		return getComments(request, pageable);
	}

	public Page<Comment> getComments(CommentSearchRequest request, Pageable pageable) {
		return commentRepository.search(request, pageable);
	}
}
