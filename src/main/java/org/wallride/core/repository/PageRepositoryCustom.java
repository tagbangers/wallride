package org.wallride.core.repository;

import org.springframework.data.domain.Pageable;
import org.wallride.core.domain.Page;

public interface PageRepositoryCustom {

	org.springframework.data.domain.Page<Page> findByFullTextSearchTerm(PageFullTextSearchTerm term, Pageable pageable);
}
