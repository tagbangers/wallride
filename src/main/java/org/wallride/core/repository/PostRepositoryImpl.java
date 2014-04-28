package org.wallride.core.repository;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;
import org.wallride.core.domain.Article;
import org.wallride.core.domain.Page;
import org.wallride.core.domain.Post;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

public class PostRepositoryImpl implements PostRepositoryCustom {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public org.springframework.data.domain.Page<Post> findByFullTextSearchTerm(PostFullTextSearchTerm term, Pageable pageable) {
		FullTextEntityManager fullTextEntityManager =  Search.getFullTextEntityManager(entityManager);

		Query query = null;
		if (StringUtils.hasText(term.getKeyword())) {
			Analyzer analyzer = fullTextEntityManager.getSearchFactory().getAnalyzer("synonyms");
			String[] fields = new String[] {
					// Post
					"title", "body",
					// Post
					"categories.name", "tags.name",
			};
			MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_36, fields, analyzer);
			parser.setDefaultOperator(Operator.AND);
			try {
				query = parser.parse(term.getKeyword());
			}
			catch (ParseException e1) {
				try {
					query = parser.parse(QueryParser.escape(term.getKeyword()));
				}
				catch (ParseException e2) {
					throw new RuntimeException(e2);
				}
			}
		}

		if (query == null) {
			return new PageImpl(new ArrayList(), pageable, 0);
		}

		Session session = (Session) entityManager.getDelegate();
		Criteria criteria = session.createCriteria(Post.class)
				.setFetchMode("cover", FetchMode.JOIN)
				.setFetchMode("author", FetchMode.JOIN);

		FullTextQuery persistenceQuery = fullTextEntityManager
				.createFullTextQuery(query, Article.class, Page.class)
				.setCriteriaQuery(criteria);
		persistenceQuery.setFirstResult(pageable.getOffset());
		persistenceQuery.setMaxResults(pageable.getPageSize());

		int resultSize = persistenceQuery.getResultSize();

		@SuppressWarnings("unchecked")
		List<Post> results = persistenceQuery.getResultList();
		return new PageImpl<>(results, pageable, resultSize);
	}
}
