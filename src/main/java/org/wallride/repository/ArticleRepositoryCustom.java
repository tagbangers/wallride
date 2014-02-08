package org.wallride.repository;

import java.util.List;

public interface ArticleRepositoryCustom {
	
	List<Long> findByFullTextSearchTerm(ArticleFullTextSearchTerm term);
}
