package org.wallride.core.repository;

import org.springframework.data.domain.Pageable;
import org.wallride.core.domain.Page;
import org.wallride.core.service.PageSearchRequest;

public interface PageRepositoryCustom {

	org.springframework.data.domain.Page<Page> search(PageSearchRequest request, Pageable pageable);
}
