package org.wallride.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.wallride.core.domain.Article;
import org.wallride.core.domain.Comment;
import org.wallride.core.service.CommentSearchRequest;

public interface CommentRepositoryCustom {

	Page<Comment> search(CommentSearchRequest request, Pageable pageable);
}
