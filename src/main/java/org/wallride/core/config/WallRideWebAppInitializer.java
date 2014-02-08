package org.wallride.core.config;

import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.Assert;
import org.springframework.web.context.AbstractContextLoaderInitializer;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.wallride.core.support.AmazonS3Resource;
import org.wallride.core.support.AmazonS3ResourceLoader;

import javax.servlet.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;

@Order(2)
public class WallRideWebAppInitializer extends AbstractContextLoaderInitializer {

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		servletContext.setInitParameter("spring.profiles.default", "develop");
		
		EnumSet<DispatcherType> dispatcherTypes = EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD);

		CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
		characterEncodingFilter.setEncoding("UTF-8");
		characterEncodingFilter.setForceEncoding(true);
		FilterRegistration.Dynamic characterEncoding = servletContext.addFilter("characterEncodingFilter", characterEncodingFilter);
		characterEncoding.addMappingForUrlPatterns(dispatcherTypes, true, "/*");

		registerAdminServlet(servletContext);
		registerGuestServlet(servletContext);

		super.onStartup(servletContext);
	}

	@Override
	protected WebApplicationContext createRootApplicationContext() {
		AnnotationConfigWebApplicationContext rootAppContext = new AnnotationConfigWebApplicationContext();
		rootAppContext.register(new Class[] {
				ComponentConfig.class,
				DataConfig.class,
				CacheConfig.class,
				SecurityConfig.class,
		});
		return rootAppContext;
	}

	private void registerAdminServlet(ServletContext servletContext) {
		AnnotationConfigWebApplicationContext servletAppContext = createServletWebApplicationContext();
		servletAppContext.register(WebAdminConfig.class);

		DispatcherServlet dispatcherServlet = new DispatcherServlet(servletAppContext);
		ServletRegistration.Dynamic registration = servletContext.addServlet("adminServlet", dispatcherServlet);
		registration.setLoadOnStartup(1);
		registration.addMapping("/_admin/*");
	}

	private void registerGuestServlet(ServletContext servletContext) {
		AnnotationConfigWebApplicationContext servletAppContext = createServletWebApplicationContext();
		servletAppContext.register(WebGuestConfig.class);

		DispatcherServlet dispatcherServlet = new DispatcherServlet(servletAppContext);
		ServletRegistration.Dynamic registration = servletContext.addServlet("guestServlet", dispatcherServlet);
		registration.setLoadOnStartup(2);
		registration.addMapping("/*");
	}

	private AnnotationConfigWebApplicationContext createServletWebApplicationContext() {
		AnnotationConfigWebApplicationContext servletAppContext = new AnnotationConfigWebApplicationContext() {
			@Override
			public Resource getResource(String location) {
				Assert.notNull(location, "Location must not be null");
				if (location.startsWith(AmazonS3ResourceLoader.S3_URL_PREFIX)) {
					String path = location.substring(AmazonS3ResourceLoader.S3_URL_PREFIX.length());
					int pos = path.indexOf('/');
					String bucketName = "";
					String key = "";
					if (pos != -1) {
						bucketName = path.substring(0, pos);
						key = path.substring(pos + 1);
					}
					else {
						bucketName = path;
					}
					return new AmazonS3Resource(getBean(AmazonS3Client.class), bucketName, key);
				}
				else if (location.startsWith(CLASSPATH_URL_PREFIX)) {
					return new ClassPathResource(location.substring(CLASSPATH_URL_PREFIX.length()), getClassLoader());
				}
				else {
					try {
						// Try to parse the location as a URL...
						URL url = new URL(location);
						return new UrlResource(url);
					}
					catch (MalformedURLException ex) {
						// No URL -> resolve as resource path.
						return getResourceByPath(location);
					}
				}

			}
		};
		return servletAppContext;
	}
}
