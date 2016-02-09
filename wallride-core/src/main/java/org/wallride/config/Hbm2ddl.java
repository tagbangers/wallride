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

package org.wallride.config;

import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.registry.internal.StandardServiceRegistryImpl;
import org.hibernate.boot.spi.MetadataImplementor;
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

		final BootstrapServiceRegistry registry = new BootstrapServiceRegistryBuilder().build();
		final MetadataSources metadataSources = new MetadataSources(registry);
		final StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder(registry);

		registryBuilder.applySetting("hibernate.dialect", ExtendedMySQL5InnoDBDialect.class.getCanonicalName());

		final PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		final Resource[] resources = resourcePatternResolver.getResources(locationPattern);
		final SimpleMetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory();
		for (Resource resource : resources) {
			MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
			AnnotationMetadata metadata = metadataReader.getAnnotationMetadata();
			if (metadata.hasAnnotation(Entity.class.getName())) {
				metadataSources.addAnnotatedClass(Class.forName(metadata.getClassName()));
			}
		}

		final StandardServiceRegistryImpl registryImpl = (StandardServiceRegistryImpl) registryBuilder.build();
		final MetadataBuilder metadataBuilder = metadataSources.getMetadataBuilder(registryImpl);

		new SchemaExport((MetadataImplementor) metadataBuilder.build())
				.setHaltOnError(true)
				.setDelimiter(";")
				.create(true, false);
	}
}
