package org.wallride.repository;

import java.util.List;

public interface UserRepositoryCustom {
	
	List<Long> findByFullTextSearchTerm(UserFullTextSearchTerm term);
}
