package org.wallride.core.service;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

	public Comment createComment(CreateCommentRequest request, AuthorizedUser createdBy) {
		Post post = postRepository.findById(request.getPostId(), request.getLanguage());
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

	public Comment updateComment() {
		return null;
	}

	public Comment deleteComment() {
		return null;
	}
}
