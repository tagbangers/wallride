package org.wallride.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.wallride.core.domain.Post;
import org.wallride.core.service.SearchPostRequest;

public interface PostRepositoryCustom {

	Page<Post> search(SearchPostRequest request, Pageable pageable);
}
