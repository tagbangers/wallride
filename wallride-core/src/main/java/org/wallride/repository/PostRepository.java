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

package org.wallride.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.wallride.domain.Post;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@Transactional
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
	
	@EntityGraph(value = Post.DEEP_GRAPH_NAME, type = EntityGraph.EntityGraphType.FETCH)
	Post findOneByIdAndLanguage(Long id, String language);

	@EntityGraph(value = Post.DEEP_GRAPH_NAME, type = EntityGraph.EntityGraphType.FETCH)
	Post findOneByCodeAndLanguage(String code, String language);

	@EntityGraph(value = Post.SHALLOW_GRAPH_NAME, type = EntityGraph.EntityGraphType.FETCH)
	List<Post> findAllByStatusAndDateLessThanEqual(Post.Status status, LocalDateTime date);

	@Query("select count(post.id) from Post post where post.language = :language ")
	long count(@Param("language") String language);

	@Query("select count(post.id) from Post post where post.status = :status and post.language = :language ")
	long countByStatus(@Param("status") Post.Status status, @Param("language") String language);
}
