package org.wallride.core.repository;

import java.util.List;

public interface UserRepositoryCustom {
	
	List<Long> findByFullTextSearchTerm(UserFullTextSearchTerm term);
}
