package org.wallride.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.wallride.core.domain.Article;
import org.wallride.core.domain.Blog;

import javax.persistence.LockModeType;

@Repository
@Transactional
public interface BlogRepository extends JpaRepository<Blog, Long> {
	
	static final String DEFAULT_SELECT_QUERY = 
			"from Blog blog " +
			"left join fetch blog.languages language ";

	@Query(DEFAULT_SELECT_QUERY + "where blog.id = :id ")
	Blog findById(@Param("id") Long id);

	@Query(DEFAULT_SELECT_QUERY + "where blog.id = :id ")
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Blog findByIdForUpdate(@Param("id") Long id);

	@Query(DEFAULT_SELECT_QUERY + "where blog.code = :code ")
	Blog findByCode(@Param("code") String code);
}
