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
import org.wallride.core.domain.User;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

public class UserRepositoryImpl implements UserRepositoryCustom {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Override
	public Page<User> findByFullTextSearchTerm(UserFullTextSearchTerm term, Pageable pageable) {
		FullTextEntityManager fullTextEntityManager =  Search.getFullTextEntityManager(entityManager);
		QueryBuilder qb = fullTextEntityManager.getSearchFactory()
				.buildQueryBuilder()
				.forEntity(User.class)
				.get();
		
		@SuppressWarnings("rawtypes")
		BooleanJunction<BooleanJunction> junction = qb.bool();
		junction.must(qb.all().createQuery());

		if (StringUtils.hasText(term.getKeyword())) {
			Analyzer analyzer = fullTextEntityManager.getSearchFactory().getAnalyzer("synonyms");
			String[] fields = new String[] {
					"loginId",
					"name.firstName", "name.lastName",
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
		
		Query searchQuery = junction.createQuery();
		
		Session session = (Session) entityManager.getDelegate();
		Criteria criteria = session.createCriteria(User.class);

		Sort sort = new Sort(new SortField("id", SortField.STRING, false));

		FullTextQuery persistenceQuery = fullTextEntityManager
				.createFullTextQuery(searchQuery, User.class)
				.setCriteriaQuery(criteria)
//				.setProjection("id")
				.setSort(sort);
		persistenceQuery.setFirstResult(pageable.getOffset());
		persistenceQuery.setMaxResults(pageable.getPageSize());

		int resultSize = persistenceQuery.getResultSize();

		@SuppressWarnings("unchecked")
		List<User> results = persistenceQuery.getResultList();
		return new PageImpl<>(results, pageable, resultSize);
	}
}
