package org.wallride.core.repository;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.springframework.util.StringUtils;
import org.wallride.core.domain.Page;
import org.wallride.core.domain.Post;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PostRepositoryImpl implements PostRepositoryCustom {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public List<Long> findByFullTextSearchTerm(PostFullTextSearchTerm term) {
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
			return new ArrayList<>();
		}

		javax.persistence.Query persistenceQuery = fullTextEntityManager.createFullTextQuery(query, Post.class, Page.class)
				.setProjection("id");

		@SuppressWarnings("unchecked")
		List<Object[]> results = persistenceQuery.getResultList();

		Set<Long> ids = new LinkedHashSet<Long>();
		for (Object[] object : results) {
			ids.add((Long) object[0]);
		}

		return new ArrayList<>(ids);
	}

}
