package org.wallride.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.wallride.core.domain.Post;
import org.wallride.core.service.PostSearchRequest;

public interface PostRepositoryCustom {

	Page<Post> search(PostSearchRequest request, Pageable pageable);
}
