/*
 * Copyright 2014 Tagbangers, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wallride.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.wallride.core.domain.Post;

import javax.persistence.LockModeType;
import java.time.LocalDateTime;
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
