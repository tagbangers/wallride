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
import org.wallride.core.domain.Tag;

import javax.persistence.LockModeType;
import java.util.Collection;
import java.util.List;

@Repository
@Transactional
public interface TagRepository extends JpaRepository<Tag, Long>, TagRepositoryCustom {
	
	static final String DEFAULT_SELECT_QUERY = 
			"from Tag tag ";

	@Query("select tag.id from Tag tag ")
	List<Long> findId();
	
	@Query(DEFAULT_SELECT_QUERY + "where tag.id in (:ids) ")
	List<Tag> findByIdIn(@Param("ids") Collection<Long> ids);
	
	@Query(DEFAULT_SELECT_QUERY + "where tag.id = :id and tag.language = :language ")
	Tag findById(@Param("id") Long id, @Param("language") String language);
	
	@Query(DEFAULT_SELECT_QUERY + "where tag.id = :id and tag.language = :language ")
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Tag findByIdForUpdate(@Param("id") Long id, @Param("language") String language);

	@Query(DEFAULT_SELECT_QUERY + "where tag.name = :name and tag.language = :language ")
	Tag findByName(@Param("name") String name, @Param("language") String language);

	@Query(DEFAULT_SELECT_QUERY + "where tag.name = :name and tag.language = :language ")
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Tag findByNameForUpdate(@Param("name") String name, @Param("language") String language);

	@Query("select count(tag.id) from Tag tag where tag.language = :language ")
	long count(@Param("language") String language);
}
