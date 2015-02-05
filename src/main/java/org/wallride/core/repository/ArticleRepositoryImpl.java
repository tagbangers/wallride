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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.util.Version;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.wallride.core.domain.Article;
import org.wallride.core.service.ArticleSearchRequest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

public class ArticleRepositoryImpl implements ArticleRepositoryCustom {
	
	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Page<Article> search(ArticleSearchRequest request) {
		return search(request, null);
	}

	@Override
	public Page<Article> search(ArticleSearchRequest request, Pageable pageable) {
		FullTextEntityManager fullTextEntityManager =  Search.getFullTextEntityManager(entityManager);
		QueryBuilder qb = fullTextEntityManager.getSearchFactory()
				.buildQueryBuilder()
				.forEntity(Article.class)
				.get();
		
		@SuppressWarnings("rawtypes")
		BooleanJunction<BooleanJunction> junction = qb.bool();
		junction.must(qb.all().createQuery());

		junction.must(qb.keyword().onField("drafted").ignoreAnalyzer().matching("_null_").createQuery());

		if (StringUtils.hasText(request.getKeyword())) {
			Analyzer analyzer = fullTextEntityManager.getSearchFactory().getAnalyzer("synonyms");
			String[] fields = new String[] {
					"title", "body",
					"categories.name", "tags.name",
			};
			MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LATEST, fields, analyzer);
			parser.setDefaultOperator(QueryParser.Operator.AND);
			Query query = null;
			try {
				query = parser.parse(request.getKeyword());
			}
			catch (ParseException e1) {
				try {
					query = parser.parse(QueryParser.escape(request.getKeyword()));
				}
				catch (ParseException e2) {
					throw new RuntimeException(e2);
				}
			}
			junction.must(query);
		}
		if (request.getStatus() != null) {
			junction.must(qb.keyword().onField("status").matching(request.getStatus()).createQuery());
		}
		if (StringUtils.hasText(request.getLanguage())) {
			junction.must(qb.keyword().onField("language").matching(request.getLanguage()).createQuery());
		}

		if (request.getDateFrom() != null) {
			junction.must(qb.range().onField("date").above(request.getDateFrom()).createQuery());
		}
		if (request.getDateTo() != null) {
			junction.must(qb.range().onField("date").below(request.getDateTo()).createQuery());
		}

		if (!CollectionUtils.isEmpty(request.getCategoryIds())) {
			BooleanJunction<BooleanJunction> subJunction = qb.bool();
			for (long categoryId : request.getCategoryIds()) {
				subJunction.should(qb.keyword().onField("categories.id").matching(categoryId).createQuery());
			}
			junction.must(subJunction.createQuery());
		}
		if (!CollectionUtils.isEmpty(request.getCategoryCodes())) {
			BooleanJunction<BooleanJunction> subJunction = qb.bool();
			for (String categoryCode : request.getCategoryCodes()) {
				subJunction.should(qb.keyword().onField("categories.code").matching(categoryCode).createQuery());
			}
			junction.must(subJunction.createQuery());
		}

		if (!CollectionUtils.isEmpty(request.getTagIds())) {
			BooleanJunction<BooleanJunction> subJunction = qb.bool();
			for (long tagId : request.getTagIds()) {
				subJunction.should(qb.keyword().onField("tags.id").matching(tagId).createQuery());
			}
			junction.must(subJunction.createQuery());
		}
		if (!CollectionUtils.isEmpty(request.getTagNames())) {
			BooleanJunction<BooleanJunction> subJunction = qb.bool();
			for (String tagName : request.getTagNames()) {
				subJunction.should(qb.keyword().onField("tags.name").matching(tagName).createQuery());
			}
			junction.must(subJunction.createQuery());
		}

		if (request.getAuthorId() != null) {
			junction.must(qb.keyword().onField("author.id").matching(request.getAuthorId()).createQuery());
		}

		Query searchQuery = junction.createQuery();
		
		Session session = (Session) entityManager.getDelegate();
		Criteria criteria = session.createCriteria(Article.class)
				.setFetchMode("cover", FetchMode.JOIN)
				.setFetchMode("user", FetchMode.JOIN)
				.setFetchMode("categories", FetchMode.JOIN);
//				.setFetchMode("tags", FetchMode.JOIN);

		Sort sort = new Sort(
				new SortField("date", SortField.Type.STRING, true),
				new SortField("id", SortField.Type.LONG, true));

		FullTextQuery persistenceQuery = fullTextEntityManager
				.createFullTextQuery(searchQuery, Article.class)
				.setCriteriaQuery(criteria)
				.setSort(sort);
		if (pageable != null) {
			persistenceQuery.setFirstResult(pageable.getOffset());
			persistenceQuery.setMaxResults(pageable.getPageSize());
		}

		int resultSize = persistenceQuery.getResultSize();

		@SuppressWarnings("unchecked")
		List<Article> results = persistenceQuery.getResultList();
		return new PageImpl<>(results, pageable, resultSize);
	}
}
