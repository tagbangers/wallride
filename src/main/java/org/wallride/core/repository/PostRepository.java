package org.wallride.core.repository;

import org.joda.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.wallride.core.domain.Post;

import javax.persistence.LockModeType;
import java.util.Collection;
import java.util.List;

@Repository
@Transactional
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
	
	static final String DEFAULT_SELECT_QUERY = 
			"from Post post " +
			"left join fetch post.cover cover " +
			"left join fetch post.author author ";

	@Query("select post.id from Post post order by post.date desc ")
	List<Long> findId();
	
	@Query(DEFAULT_SELECT_QUERY + "where post.id in (:ids) and post.language = :language ")
	List<Post> findByIdIn(@Param("ids") Collection<Long> ids, @Param("language") String language);

	@Query(DEFAULT_SELECT_QUERY + "where post.status = :status and post.date <= :date ")
	List<Post> findByStatusAndDateLessThanEqual(@Param("status") Post.Status status, @Param("date") LocalDateTime date);

	@Query(DEFAULT_SELECT_QUERY + "where post.id = :id and post.language = :language ")
	Post findById(@Param("id") Long id, @Param("language") String language);
	
	@Query(DEFAULT_SELECT_QUERY + "where post.id = :id and post.language = :language ")
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Post findByIdForUpdate(@Param("id") Long id, @Param("language") String language);

	@Query(DEFAULT_SELECT_QUERY + "where post.code = :code and post.language = :language ")
	Post findByCode(@Param("code") String code, @Param("language") String language);

	@Query("select count(post.id) from Post post where post.language = :language ")
	long count(@Param("language") String language);

	@Query("select count(post.id) from Post post where post.status = :status and post.language = :language ")
	long countByStatus(@Param("status") Post.Status status, @Param("language") String language);
}
