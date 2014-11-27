package org.wallride.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.wallride.core.domain.Comment;

import javax.persistence.LockModeType;

@Repository
@Transactional
public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {

	static final String DEFAULT_OBJECT_SELECT_QUERY =
			"from Comment comment " +
			"left join fetch comment.author author ";

	@Query(DEFAULT_OBJECT_SELECT_QUERY + "where comment.id = :id ")
	Comment findById(@Param("id") Long id);

	@Query(DEFAULT_OBJECT_SELECT_QUERY + "where comment.id = :id ")
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Comment findByIdForUpdate(@Param("id") Long id);
}
