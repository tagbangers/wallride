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

package org.wallride;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.cloud.aws.core.io.s3.PathMatchingSimpleStorageResourcePatternResolver;
import org.springframework.cloud.aws.core.io.s3.SimpleStorageResourceLoader;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.StandardServletEnvironment;
import org.wallride.autoconfigure.WallRideProperties;

//@Configuration
//@EnableAutoConfiguration(exclude = {
//		DispatcherServletAutoConfiguration.class,
//		WebMvcAutoConfiguration.class,
//		SpringDataWebAutoConfiguration.class,
//})


//@EnableConfigurationProperties(WallRideProperties.class)
//@ComponentScan(basePackageClasses = CoreConfig.class, includeFilters = @ComponentScan.Filter(Configuration.class))
public class WallRideInitializer /*extends SpringBootServletInitializer*/ {

	public static SpringApplicationBuilder initialize() {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(WallRideInitializer.class);
		builder.contextClass(AnnotationConfigEmbeddedWebApplicationContext.class);
		return initialize(builder);
	}

	public static SpringApplicationBuilder initialize(SpringApplicationBuilder builder) {
		ConfigurableEnvironment environment = new StandardServletEnvironment();
		String home = environment.getProperty(WallRideProperties.HOME_PROPERTY);
		if (!StringUtils.hasText(home)) {
			throw new IllegalStateException(WallRideProperties.HOME_PROPERTY + " is empty");
		}
		if (!home.endsWith("/")) {
			home = home + "/";
		}

		String config = home + WallRideProperties.DEFAULT_CONFIG_PATH_NAME;
		String media = home + WallRideProperties.DEFAULT_MEDIA_PATH_NAME;

		System.setProperty(WallRideProperties.CONFIG_LOCATION_PROPERTY, config);
		System.setProperty(WallRideProperties.MEDIA_LOCATION_PROPERTY, media);

		System.setProperty(ConfigFileApplicationListener.CONFIG_LOCATION_PROPERTY, config);

		ResourceLoader resourceLoader = createResourceLoader();
		return builder.sources(WallRideInitializer.class)
				.environment(environment)
				.resourceLoader(resourceLoader);

	}

	public static ResourceLoader createResourceLoader() {
		ClientConfiguration configuration = new ClientConfiguration();
		configuration.setMaxConnections(1000);
		AmazonS3 amazonS3 = new AmazonS3Client(configuration);

		SimpleStorageResourceLoader resourceLoader = new SimpleStorageResourceLoader(amazonS3);
		try {
			resourceLoader.afterPropertiesSet();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return new PathMatchingSimpleStorageResourcePatternResolver(amazonS3, resourceLoader, new PathMatchingResourcePatternResolver());
	}

//	@Override
//	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
//		return initialize(builder);
//	}

//	@Override
//	protected WebApplicationContext createRootApplicationContext(ServletContext servletContext) {
//		SpringApplicationBuilder application = new SpringApplicationBuilder();
//		application.initializers(new ServletContextApplicationContextInitializer(servletContext));
//		application.contextClass(AnnotationConfigEmbeddedWebApplicationContext.class);
//		application = configure(application);
//		// Ensure error pages are registered
////		application.sources(ErrorPageFilter.class);
//		return (WebApplicationContext) application.run();
//	}
}
