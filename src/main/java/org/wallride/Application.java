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
import org.apache.lucene.analysis.util.ClasspathResourceLoader;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.config.ConfigFileEnvironmentPostProcessor;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.web.ServletContextApplicationContextInitializer;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.cloud.aws.core.io.s3.PathMatchingSimpleStorageResourcePatternResolver;
import org.springframework.cloud.aws.core.io.s3.SimpleStorageResourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.ServletContextResourcePatternResolver;
import org.springframework.web.context.support.StandardServletEnvironment;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;
import org.wallride.config.CoreConfig;
import org.wallride.core.support.WallRideProperties;
import org.wallride.web.WebAdminConfig;
import org.wallride.web.WebGuestConfig;
import org.wallride.web.support.ExtendedUrlRewriteFilter;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.EnumSet;

@Configuration
@EnableAutoConfiguration(exclude = {
		DispatcherServletAutoConfiguration.class,
		WebMvcAutoConfiguration.class,
		SpringDataWebAutoConfiguration.class,
})
@EnableConfigurationProperties(WallRideProperties.class)
@ComponentScan(basePackageClasses = CoreConfig.class, includeFilters = @ComponentScan.Filter(Configuration.class))
public class Application extends SpringBootServletInitializer {

	public static final String GUEST_SERVLET_NAME = "guestServlet";
	public static final String GUEST_SERVLET_PATH = "";

	public static final String ADMIN_SERVLET_NAME = "adminServlet";
	public static final String ADMIN_SERVLET_PATH = "/_admin";

	public static void main(String[] args) throws Exception {
		ConfigurableEnvironment environment = new StandardServletEnvironment();
		initialize(environment);
		ResourceLoader resourceLoader = createResourceLoader();
		new SpringApplicationBuilder(Application.class)
				.contextClass(AnnotationConfigEmbeddedWebApplicationContext.class)
				.environment(environment)
				.resourceLoader(resourceLoader)
				.run(args);
	}

	public static void initialize(ConfigurableEnvironment environment) {
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

		System.setProperty(ConfigFileEnvironmentPostProcessor.CONFIG_LOCATION_PROPERTY, config);

		URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
			@Override
			public URLStreamHandler createURLStreamHandler(String protocol) {
				return null;
			}
		});
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

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		ConfigurableEnvironment environment = new StandardServletEnvironment();
		initialize(environment);
		ResourceLoader resourceLoader = createResourceLoader();
		return application.sources(Application.class)
				.environment(environment)
				.resourceLoader(resourceLoader);
	}

	@Override
	protected WebApplicationContext createRootApplicationContext(ServletContext servletContext) {
		SpringApplicationBuilder application = new SpringApplicationBuilder();
		application.initializers(new ServletContextApplicationContextInitializer(servletContext));
		application.contextClass(AnnotationConfigEmbeddedWebApplicationContext.class);
		application = configure(application);
		// Ensure error pages are registered
//		application.sources(ErrorPageFilter.class);
		return (WebApplicationContext) application.run();
	}

	@Bean
	public FilterRegistrationBean characterEncodingFilter() {
		CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
		characterEncodingFilter.setEncoding("UTF-8");
		characterEncodingFilter.setForceEncoding(true);

		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setName("characterEncodingFilter");
		registration.setFilter(characterEncodingFilter);
		registration.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD));
		registration.addUrlPatterns("/*");
		registration.setOrder(1);
		return registration;
	}

	@Bean
	public FilterRegistrationBean hiddenHttpMethodFilter() {
		HiddenHttpMethodFilter hiddenHttpMethodFilter = new HiddenHttpMethodFilter();

		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setName("hiddenHttpMethodFilter");
		registration.setFilter(hiddenHttpMethodFilter);
		registration.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD));
		registration.addUrlPatterns("/*");
		registration.setOrder(2);
		return registration;
	}

	@Bean
	public FilterRegistrationBean urlRewriteFilter() {
		UrlRewriteFilter urlRewriteFilter = new ExtendedUrlRewriteFilter();

		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setName("urlRewriteFilter");
		registration.setFilter(urlRewriteFilter);
		registration.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
		registration.addUrlPatterns("/*");
		registration.setOrder(3);
		registration.getInitParameters().put("confPath", "classpath:/urlrewrite.xml");
		return registration;
	}

	@Bean
	public ServletRegistrationBean registerAdminServlet(ResourceLoader resourceLoader) {
		AnnotationConfigEmbeddedWebApplicationContext context = new AnnotationConfigEmbeddedWebApplicationContext();
		context.setResourceLoader(resourceLoader);
		context.register(WebAdminConfig.class);

		DispatcherServlet dispatcherServlet = new DispatcherServlet(context);

		ServletRegistrationBean registration = new ServletRegistrationBean(dispatcherServlet);
		registration.setName(ADMIN_SERVLET_NAME);
		registration.setLoadOnStartup(1);
		registration.addUrlMappings(ADMIN_SERVLET_PATH + "/*");
		return registration;
	}

	@Bean
	public ServletRegistrationBean registerGuestServlet(ResourceLoader resourceLoader) {
		AnnotationConfigEmbeddedWebApplicationContext context = new AnnotationConfigEmbeddedWebApplicationContext();
		context.setResourceLoader(resourceLoader);
		context.register(WebGuestConfig.class);

		DispatcherServlet dispatcherServlet = new DispatcherServlet(context);

		ServletRegistrationBean registration = new ServletRegistrationBean(dispatcherServlet);
//		registration.setName(DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME);
		registration.setName(GUEST_SERVLET_NAME);
		registration.setLoadOnStartup(2);
		registration.addUrlMappings(GUEST_SERVLET_PATH + "/*");
		return registration;
	}

	@Bean
	public RequestContextListener requestContextListener() {
		return new RequestContextListener();
	}

//	public static class ExtendedAnnotationConfigEmbeddedWebApplicationContext extends AnnotationConfigEmbeddedWebApplicationContext {
//
//		@Override
//		public Resource getResource(String location) {
//			Assert.notNull(location, "Location must not be null");
//			if (location.startsWith(AmazonS3ResourceLoader.S3_URL_PREFIX)) {
//				String path = location.substring(AmazonS3ResourceLoader.S3_URL_PREFIX.length());
//				int pos = path.indexOf('/');
//				String bucketName = "";
//				String key = "";
//				if (pos != -1) {
//					bucketName = path.substring(0, pos);
//					key = path.substring(pos + 1);
//				} else {
//					bucketName = path;
//				}
//				return new AmazonS3Resource(getBean(AmazonS3Client.class), bucketName, key);
//			} else if (location.startsWith(CLASSPATH_URL_PREFIX)) {
//				return new ClassPathResource(location.substring(CLASSPATH_URL_PREFIX.length()), getClassLoader());
//			} else {
//				try {
//					// Try to parse the location as a URL...
//					URL url = new URL(location);
//					return new UrlResource(url);
//				} catch (MalformedURLException ex) {
//					// No URL -> resolve as resource path.
//					return getResourceByPath(location);
//				}
//			}
//		}
//	}
}
