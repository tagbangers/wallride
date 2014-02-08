package org.wallride.web.admin;

import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Controller
@RequestMapping("/indexing")
public class IndexingController {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@RequestMapping()
	public String index() throws InterruptedException {
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
//		fullTextEntityManager.createIndexer().startAndWait();
		fullTextEntityManager.createIndexer().start();
		return "/indexing";
	}
}
