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

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.wallride.domain.Article;
import org.wallride.domain.Post;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public interface ArticleRepository extends JpaRepository<Article, Long>, ArticleRepositoryCustom {

	@EntityGraph(value = Article.DEEP_GRAPH_NAME, type = EntityGraph.EntityGraphType.FETCH)
	Article findOne(Specification<Article> spec);

	@EntityGraph(value = Article.DEEP_GRAPH_NAME, type = EntityGraph.EntityGraphType.FETCH)
	Article findOneByIdAndLanguage(Long id, String language);

	@EntityGraph(value = Article.DEEP_GRAPH_NAME, type = EntityGraph.EntityGraphType.FETCH)
	Article findOneByCodeAndLanguage(String code, String language);

	@EntityGraph(value = Article.SHALLOW_GRAPH_NAME, type = EntityGraph.EntityGraphType.FETCH)
	List<Article> findAllByIdIn(Collection<Long> ids);

	@Query("select count(article.id) from Article article where article.language = :language and article.drafted is null ")
	long count(@Param("language") String language);

	@Query("select count(article.id) from Article article where article.status = :status and article.language = :language and article.drafted is null ")
	long countByStatus(@Param("status") Post.Status status, @Param("language") String language);

	@Query(
			"select new map(user.id as userId, count(article.id) as count) from Article article " +
			"left join article.author user " +
			"where article.status = :status and article.language = :language " +
			"group by user.id ")
	List<Map<String, Object>> countByAuthorIdGrouped(@Param("status") Post.Status status, @Param("language") String language);

	@Query(
			"select new map(category.id as categoryId, count(article.id) as count) from Article article " +
			"left join article.categories category " +
			"where article.status = :status and article.language = :language " +
			"group by category.id ")
	List<Map<String, Object>> countByCategoryIdGrouped(@Param("status") Post.Status status, @Param("language") String language);

	@Query(
			"select new map(tag.id as tagId, count(article.id) as count) from Article article " +
			"left join article.tags tag " +
			"where article.status = :status and article.language = :language " +
			"group by tag.id ")
	List<Map<String, Object>> countByTagIdGrouped(@Param("status") Post.Status status, @Param("language") String language);

	@Modifying
	@Query("delete from Article article where article.drafted = :drafted ")
	void deleteByDrafted(@Param("drafted") Article dradted);
}