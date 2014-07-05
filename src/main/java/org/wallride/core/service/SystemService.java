package org.wallride.core.service;

import org.hibernate.*;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.annotations.Indexed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

@Service
public class SystemService {

	private static final int BATCH_SIZE = 100;

	private static Logger logger = LoggerFactory.getLogger(SystemService.class);

	@PersistenceContext
	private EntityManager entityManager;

	@Async
	@Transactional(propagation = Propagation.SUPPORTS)
	public void reIndex() throws Exception {
		logger.info("Re-Index started");

		final List<Class> persistentClasses = detectPersistentClasses();

		FullTextSession fullTextSession = Search.getFullTextSession((entityManager.unwrap(Session.class)));

		fullTextSession.setFlushMode(FlushMode.MANUAL);
		fullTextSession.setCacheMode(CacheMode.IGNORE);

		for (Class persistentClass : persistentClasses) {
			Transaction transaction = fullTextSession.beginTransaction();

			// Scrollable results will avoid loading too many objects in memory
			ScrollableResults results = fullTextSession.createCriteria(persistentClass)
					.setFetchSize(BATCH_SIZE)
					.scroll(ScrollMode.FORWARD_ONLY);
			int index = 0;
			while (results.next()) {
				index++;
				fullTextSession.index(results.get(0)); //index each element
				if (index % BATCH_SIZE == 0) {
					fullTextSession.flushToIndexes(); //apply changes to indexes
					fullTextSession.clear(); //free memory since the queue is processed
				}
			}
			transaction.commit();
		}
		logger.info("Re-Index finished");
	}

	private List<Class> detectPersistentClasses() throws Exception {
		String locationPattern = "classpath:/org/wallride/core/domain/*";

		List<Class> classes = new ArrayList<>();
		PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = resourcePatternResolver.getResources(locationPattern);
		SimpleMetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory();
		for (Resource resource : resources) {
			MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
			AnnotationMetadata metadata = metadataReader.getAnnotationMetadata();
			if (metadata.hasAnnotation(Indexed.class.getName())) {
				classes.add(Class.forName(metadata.getClassName()));
			}
		}
		return classes;
	}
}
