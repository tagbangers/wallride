package org.wallride.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.wallride.core.domain.Article;
import org.wallride.core.service.ArticleSearchRequest;

public interface ArticleRepositoryCustom {

	Page<Article> search(ArticleSearchRequest request);
	Page<Article> search(ArticleSearchRequest request, Pageable pageable);
}
