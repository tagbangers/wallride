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
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.wallride.core.domain.PopularPost;
import org.wallride.core.domain.Post;

import java.util.SortedSet;

@Repository
@Transactional
public interface PopularPostRepository extends JpaRepository<PopularPost, Long> {

	static final String DEFAULT_SELECT_QUERY =
			"from PopularPost popularPost " +
			"left join fetch popularPost.post post " +
			"left join fetch post.cover cover " +
			"left join fetch post.author author ";

//	@Query(DEFAULT_SELECT_QUERY + "where popularPost.language = :language and popularPost.type = :type ")
//	SortedSet<PopularPost> findByType(@Param("language") String language, @Param("type") PopularPost.Type type);

	@Query(DEFAULT_SELECT_QUERY + "where popularPost.language = :language and popularPost.type = :type and post.status = :status ")
	SortedSet<PopularPost> findByType(@Param("language") String language, @Param("type") PopularPost.Type type, @Param("status") Post.Status status);

	@Modifying
	@Query("delete from PopularPost popularPost where popularPost.language = :language and popularPost.type = :type")
	void deleteByType(@Param("language") String language, @Param("type") PopularPost.Type type);
}
