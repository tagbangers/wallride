package org.wallride.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.wallride.core.domain.Category;
import org.wallride.core.service.CategorySearchRequest;

public interface CategoryRepositoryCustom {

	Page<Category> search(CategorySearchRequest request, Pageable pageable);
}
