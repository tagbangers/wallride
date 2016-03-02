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

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.wallride.core.domain.Page;
import org.wallride.core.domain.Post;

import java.util.Collection;
import java.util.List;

@Repository
@Transactional
public interface PageRepository extends JpaRepository<Page, Long>, PageRepositoryCustom {

	@EntityGraph(value = Page.DEEP_GRAPH_NAME, type = EntityGraph.EntityGraphType.FETCH)
	Page findOne(Specification<Page> spec);

	@EntityGraph(value = Page.DEEP_GRAPH_NAME, type = EntityGraph.EntityGraphType.FETCH)
	Page findOneByIdAndLanguage(Long id, String language);

	@EntityGraph(value = Page.DEEP_GRAPH_NAME, type = EntityGraph.EntityGraphType.FETCH)
	Page findOneByCodeAndLanguage(String code, String language);

	@EntityGraph(value = Page.SHALLOW_GRAPH_NAME, type = EntityGraph.EntityGraphType.FETCH)
	List<Page> findAll(Specification<Page> spec);

	@EntityGraph(value = Page.SHALLOW_GRAPH_NAME, type = EntityGraph.EntityGraphType.FETCH)
	List<Page> findAllByIdIn(Collection<Long> ids);

	@Query("select count(page.id) from Page page where page.language = :language and page.drafted is null ")
	long count(@Param("language") String language);

	@Query("select count(page.id) from Page page where page.status = :status and page.language = :language and page.drafted is null ")
	long countByStatus(@Param("status") Post.Status status, @Param("language") String language);

	@Query("select coalesce(max(rgt), 0) from Page ")
	int findMaxRgt();

	@Modifying
	@Query("update Page set lft = lft + 2 where lft > :rgt ")
	void unshiftLft(@Param("rgt") int rgt);

	@Modifying
	@Query("update Page set rgt = rgt + 2 where rgt >= :rgt ")
	void unshiftRgt(@Param("rgt") int rgt);

	@Modifying
	@Query("update Page set rgt = rgt - 1, lft = lft - 1 where lft between :lft and :rgt ")
	void shiftLftRgt(@Param("lft") int lft, @Param("rgt") int rgt);

	@Modifying
	@Query("update Page set lft = lft - 2 where lft > :rgt ")
	void shiftLft(@Param("rgt") int rgt);

	@Modifying
	@Query("update Page set rgt = rgt - 2 where rgt > :rgt ")
	void shiftRgt(@Param("rgt") int rgt);

	@Modifying
	@Query("delete from Page page where page.drafted = :drafted ")
	void deleteByDrafted(@Param("drafted") Page dradted);
}
