package org.wallride.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.wallride.core.domain.Article;

public interface ArticleRepositoryCustom {

	Page<Article> findByFullTextSearchTerm(ArticleFullTextSearchTerm term, Pageable pageable);
}
