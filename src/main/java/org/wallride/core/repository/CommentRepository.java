package org.wallride.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.wallride.core.domain.Blog;
import org.wallride.core.domain.Comment;

import javax.persistence.LockModeType;

@Repository
@Transactional
public interface CommentRepository extends JpaRepository<Comment, Long> {
	
}
