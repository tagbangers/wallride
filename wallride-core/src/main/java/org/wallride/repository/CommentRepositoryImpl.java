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
import org.wallride.domain.Comment;
import org.wallride.model.CommentSearchRequest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction;
import static org.springframework.data.domain.Sort.Order;

public class CommentRepositoryImpl implements CommentRepositoryCustom {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Page<Comment> search(CommentSearchRequest request, Pageable pageable) {
		FullTextEntityManager fullTextEntityManager =  Search.getFullTextEntityManager(entityManager);
		QueryBuilder qb = fullTextEntityManager.getSearchFactory()
				.buildQueryBuilder()
				.forEntity(Comment.class)
				.get();

		@SuppressWarnings("rawtypes")
		BooleanJunction<BooleanJunction> junction = qb.bool();
		junction.must(qb.all().createQuery());

		if (StringUtils.hasText(request.getKeyword())) {
			Analyzer analyzer = fullTextEntityManager.getSearchFactory().getAnalyzer("synonyms");
			String[] fields = new String[] {
					"authorName", "content"
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
			junction.must(qb.keyword().onField("post.language").matching(request.getLanguage()).createQuery());
		}

		if (request.getPostId() != null) {
			junction.must(qb.keyword().onField("post.id").matching(request.getPostId()).createQuery());
		}

		if (request.getApproved() != null) {
			junction.must(qb.keyword().onField("approved").matching(request.getApproved()).createQuery());
		}

		Query searchQuery = junction.createQuery();

		Session session = (Session) entityManager.getDelegate();
		Criteria criteria = session.createCriteria(Comment.class)
				.setFetchMode("post", FetchMode.JOIN)
				.setFetchMode("author", FetchMode.JOIN);

		Sort sort = null;
		if (pageable.getSort() != null) {
			if (pageable.getSort().getOrderFor("date") != null) {
				Order order = pageable.getSort().getOrderFor("date");
				sort = new Sort(
						new SortField("date", SortField.Type.STRING, order.getDirection().equals(Direction.DESC)),
						new SortField("id", SortField.Type.LONG, order.getDirection().equals(Direction.DESC)));
			}
		}

		if (sort == null) {
			sort = new Sort(
					new SortField("date", SortField.Type.STRING),
					new SortField("id", SortField.Type.LONG));
		}

		FullTextQuery persistenceQuery = fullTextEntityManager
				.createFullTextQuery(searchQuery, Comment.class)
				.setCriteriaQuery(criteria)
				.setSort(sort);
		persistenceQuery.setFirstResult(pageable.getOffset());
		persistenceQuery.setMaxResults(pageable.getPageSize());

		int resultSize = persistenceQuery.getResultSize();

		@SuppressWarnings("unchecked")
		List<Comment> results = persistenceQuery.getResultList();
		return new PageImpl<>(results, pageable, resultSize);
	}
}
