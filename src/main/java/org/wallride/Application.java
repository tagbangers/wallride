package org.wallride;

import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.web.ServletContextApplicationContextInitializer;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.wallride.config.CoreConfig;
import org.wallride.core.support.AmazonS3Resource;
import org.wallride.core.support.AmazonS3ResourceLoader;
import org.wallride.web.WebAdminConfig;
import org.wallride.web.WebGuestConfig;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;

@Configuration
@EnableAutoConfiguration(exclude = {DispatcherServletAutoConfiguration.class, WebMvcAutoConfiguration.class, ThymeleafAutoConfiguration.class})
@ComponentScan(basePackageClasses = CoreConfig.class, includeFilters = @ComponentScan.Filter(Configuration.class))
public class Application extends SpringBootServletInitializer {

	public static void main(String[] args) {
//		SpringApplication.run(Application.class, args);
		new SpringApplicationBuilder(Application.class)
				.contextClass(ExtendedAnnotationConfigEmbeddedWebApplicationContext.class)
				.run(args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

	@Override
	protected WebApplicationContext createRootApplicationContext(ServletContext servletContext) {
		SpringApplicationBuilder application = new SpringApplicationBuilder();
		application.initializers(new ServletContextApplicationContextInitializer(servletContext));
		application.contextClass(ExtendedAnnotationConfigEmbeddedWebApplicationContext.class);
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
		return registration;
	}

	@Bean
	public ServletRegistrationBean registerAdminServlet() {
		AnnotationConfigEmbeddedWebApplicationContext context = new ExtendedAnnotationConfigEmbeddedWebApplicationContext();
		context.register(WebAdminConfig.class);

		DispatcherServlet dispatcherServlet = new DispatcherServlet(context);

		ServletRegistrationBean registration = new ServletRegistrationBean(dispatcherServlet);
		registration.setName("adminServlet");
		registration.setLoadOnStartup(1);
		registration.addUrlMappings("/_admin/*");
		return registration;
	}

	@Bean
	public ServletRegistrationBean registerGuestServlet() {
		AnnotationConfigEmbeddedWebApplicationContext context = new ExtendedAnnotationConfigEmbeddedWebApplicationContext();
		context.register(WebGuestConfig.class);

		DispatcherServlet dispatcherServlet = new DispatcherServlet(context);

		ServletRegistrationBean registration = new ServletRegistrationBean(dispatcherServlet);
//		registration.setName(DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME);
		registration.setName("guestServlet");
		registration.setLoadOnStartup(2);
		registration.addUrlMappings("/*");
		return registration;
	}

	public static class ExtendedAnnotationConfigEmbeddedWebApplicationContext extends AnnotationConfigEmbeddedWebApplicationContext {

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
				} else {
					bucketName = path;
				}
				return new AmazonS3Resource(getBean(AmazonS3Client.class), bucketName, key);
			} else if (location.startsWith(CLASSPATH_URL_PREFIX)) {
				return new ClassPathResource(location.substring(CLASSPATH_URL_PREFIX.length()), getClassLoader());
			} else {
				try {
					// Try to parse the location as a URL...
					URL url = new URL(location);
					return new UrlResource(url);
				} catch (MalformedURLException ex) {
					// No URL -> resolve as resource path.
					return getResourceByPath(location);
				}
			}
		}
	}
}
