package org.wallride.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.wallride.core.domain.Tag;
import org.wallride.core.service.TagSearchRequest;

public interface TagRepositoryCustom {

	Page<Tag> search(TagSearchRequest request, Pageable pageable);
}
