package org.wallride.core.repository;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

public class ArticleRepositoryImpl implements ArticleRepositoryCustom {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Override
	public Page<Article> findByFullTextSearchTerm(ArticleFullTextSearchTerm term, Pageable pageable) {
		FullTextEntityManager fullTextEntityManager =  Search.getFullTextEntityManager(entityManager);
		QueryBuilder qb = fullTextEntityManager.getSearchFactory()
				.buildQueryBuilder()
				.forEntity(Article.class)
				.get();
		
		@SuppressWarnings("rawtypes")
		BooleanJunction<BooleanJunction> junction = qb.bool();
		junction.must(qb.all().createQuery());

		if (StringUtils.hasText(term.getKeyword())) {
			Analyzer analyzer = fullTextEntityManager.getSearchFactory().getAnalyzer("synonyms");
			String[] fields = new String[] {
					"title", "body",
					"categories.name", "tags.name",
			};
			MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_36, fields, analyzer);
			parser.setDefaultOperator(Operator.AND);
			Query query = null;
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
			junction.must(query);
		}
		if (term.getStatus() != null) {
			junction.must(qb.keyword().onField("status").matching(term.getStatus()).createQuery());
		}
		if (StringUtils.hasText(term.getLanguage())) {
			junction.must(qb.keyword().onField("language").matching(term.getLanguage()).createQuery());
		}

		if (term.getDateFrom() != null) {
			junction.must(qb.range().onField("date").above(term.getDateFrom()).createQuery());
		}
		if (term.getDateTo() != null) {
			junction.must(qb.range().onField("date").below(term.getDateTo()).createQuery());
		}

		if (!CollectionUtils.isEmpty(term.getCategoryIds())) {
			for (long categoryId : term.getCategoryIds()) {
				junction.must(qb.keyword().onField("categories.id").matching(categoryId).createQuery());
			}
		}
		if (!CollectionUtils.isEmpty(term.getCategoryCodes())) {
			for (String categoryCode : term.getCategoryCodes()) {
				junction.must(qb.keyword().onField("categories.code").matching(categoryCode).createQuery());
			}
		}

		Query searchQuery = junction.createQuery();
		
		Session session = (Session) entityManager.getDelegate();
		Criteria criteria = session.createCriteria(Article.class)
				.setFetchMode("cover", FetchMode.JOIN)
				.setFetchMode("author", FetchMode.JOIN)
				.setFetchMode("categories", FetchMode.JOIN);

		Sort sort = new Sort(new SortField("date", SortField.STRING, true));

		FullTextQuery persistenceQuery = fullTextEntityManager
				.createFullTextQuery(searchQuery, Article.class)
				.setCriteriaQuery(criteria)
//				.setProjection("id")
				.setSort(sort);
		persistenceQuery.setFirstResult(pageable.getOffset());
		persistenceQuery.setMaxResults(pageable.getPageSize());

		int resultSize = persistenceQuery.getResultSize();

		@SuppressWarnings("unchecked")
		List<Article> results = persistenceQuery.getResultList();
		return new PageImpl<>(results, pageable, resultSize);
	}
}
