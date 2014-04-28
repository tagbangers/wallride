package org.wallride.core.repository;

import org.springframework.data.domain.Pageable;
import org.wallride.core.domain.Post;

public interface PostRepositoryCustom {

	org.springframework.data.domain.Page<Post> findByFullTextSearchTerm(PostFullTextSearchTerm term, Pageable pageable);
}
