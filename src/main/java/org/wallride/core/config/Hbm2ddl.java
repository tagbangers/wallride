package org.wallride.core.config;

import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.MySQL5InnoDBDialect;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;

import javax.persistence.Entity;

public class Hbm2ddl {

	public static void main(String[] args) throws Exception {
		String locationPattern = "classpath:/org/wallride/core/domain/*";

		Configuration configuration = new Configuration()
				.setProperty(Environment.DIALECT, MySQL5InnoDBDialect.class.getCanonicalName());

		PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = resourcePatternResolver.getResources(locationPattern);
		SimpleMetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory();
		for (Resource resource : resources) {
			MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
			AnnotationMetadata metadata = metadataReader.getAnnotationMetadata();
			if (metadata.hasAnnotation(Entity.class.getName())) {
				configuration.addAnnotatedClass(Class.forName(metadata.getClassName()));
			}
		}

		new SchemaExport(configuration)
				.setDelimiter(";")
				.create(true, false);
		System.out.println(System.getProperty("test2"));
	}
}
