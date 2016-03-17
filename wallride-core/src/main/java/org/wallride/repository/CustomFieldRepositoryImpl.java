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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
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
import org.springframework.util.StringUtils;
import org.wallride.domain.CustomField;
import org.wallride.domain.CustomField_;
import org.wallride.model.CustomFieldSearchRequest;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.stream.Collectors;

public class CustomFieldRepositoryImpl implements CustomFieldRepositoryCustom {
	
	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public void lock(long id) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<CustomField> root = query.from(CustomField.class);
		query.select(root.get(CustomField_.id));
		query.where(cb.equal(root.get(CustomField_.id), id));
		entityManager.createQuery(query).setLockMode(LockModeType.PESSIMISTIC_WRITE).getSingleResult();
	}

	@Override
	public Page<CustomField> search(CustomFieldSearchRequest request) {
		return search(request, null);
	}

	@Override
	public Page<CustomField> search(CustomFieldSearchRequest request, Pageable pageable) {
		Session session = (Session) entityManager.getDelegate();
		Criteria criteria = session.createCriteria(CustomField.class)
				.setFetchMode("options", FetchMode.JOIN);

		FullTextQuery persistenceQuery = buildFullTextQuery(request, pageable, criteria);
		int resultSize = persistenceQuery.getResultSize();
		List<CustomField> results = persistenceQuery.getResultList();
		return new PageImpl<>(results, pageable, resultSize);
	}

	@Override
	public List<Long> searchForId(CustomFieldSearchRequest request) {
		FullTextQuery persistenceQuery = buildFullTextQuery(request, null, null);
		persistenceQuery.setProjection("id");
		List<Object[]> results = persistenceQuery.getResultList();
		List<Long> nos = results.stream().map(result -> (long) result[0]).collect(Collectors.toList());
		return nos;
	}

	public FullTextQuery buildFullTextQuery(CustomFieldSearchRequest request, Pageable pageable, Criteria criteria) {
		FullTextEntityManager fullTextEntityManager =  Search.getFullTextEntityManager(entityManager);
		QueryBuilder qb = fullTextEntityManager.getSearchFactory()
				.buildQueryBuilder()
				.forEntity(CustomField.class)
				.get();
		
		@SuppressWarnings("rawtypes")
		BooleanJunction<BooleanJunction> junction = qb.bool();
		junction.must(qb.all().createQuery());

		if (StringUtils.hasText(request.getKeyword())) {
			Analyzer analyzer = fullTextEntityManager.getSearchFactory().getAnalyzer("synonyms");
			String[] fields = new String[] {
					"name", "code", "description"
			};
			MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer);
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

		if (StringUtils.hasText(request.getLanguage())) {
			junction.must(qb.keyword().onField("language").matching(request.getLanguage()).createQuery());
		}

		Query searchQuery = junction.createQuery();
		
		Sort sort = new Sort(new SortField("idx", SortField.Type.INT));

		FullTextQuery persistenceQuery = fullTextEntityManager
				.createFullTextQuery(searchQuery, CustomField.class)
				.setCriteriaQuery(criteria)
				.setSort(sort);
		if (pageable != null) {
			persistenceQuery.setFirstResult(pageable.getOffset());
			persistenceQuery.setMaxResults(pageable.getPageSize());
		}
		return persistenceQuery;
	}
}