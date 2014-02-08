package org.wallride.repository;

import java.util.List;

public interface PostRepositoryCustom {
	
	List<Long> findByFullTextSearchTerm(PostFullTextSearchTerm term);
}
