package org.wallride.core.repository;

import java.util.List;

public interface PageRepositoryCustom {
	
	List<Long> findByFullTextSearchTerm(PageFullTextSearchTerm term);
}
