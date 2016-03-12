package org.wallride.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.web.OrderedHiddenHttpMethodFilter;
import org.springframework.boot.context.web.OrderedHttpPutFormContentFilter;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.filter.HttpPutFormContentFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;
import org.wallride.web.support.ExtendedUrlRewriteFilter;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

@Configuration
public class WallRideServletConfiguration implements ResourceLoaderAware {

	public static final String GUEST_SERVLET_NAME = "guestServlet";
	public static final String GUEST_SERVLET_PATH = "";

	public static final String ADMIN_SERVLET_NAME = "adminServlet";
	public static final String ADMIN_SERVLET_PATH = "/_admin";

	private ResourceLoader resourceLoader;

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Bean
	@ConditionalOnMissingBean(HiddenHttpMethodFilter.class)
	public OrderedHiddenHttpMethodFilter hiddenHttpMethodFilter() {
		return new OrderedHiddenHttpMethodFilter();
	}

	@Bean
	@ConditionalOnMissingBean(HttpPutFormContentFilter.class)
	public OrderedHttpPutFormContentFilter httpPutFormContentFilter() {
		return new OrderedHttpPutFormContentFilter();
	}

	@Bean
	public FilterRegistrationBean urlRewriteFilter() {
		UrlRewriteFilter urlRewriteFilter = new ExtendedUrlRewriteFilter();

		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setName("urlRewriteFilter");
		registration.setFilter(urlRewriteFilter);
		registration.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
		registration.addUrlPatterns("/*");
		registration.setOrder(0);
		registration.getInitParameters().put("confPath", "classpath:/urlrewrite.xml");
		return registration;
	}

	@Bean(name = DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
	public DispatcherServlet guestDispatcherServlet() {
		AnnotationConfigEmbeddedWebApplicationContext context = new AnnotationConfigEmbeddedWebApplicationContext();
		context.setResourceLoader(getResourceLoader());
		context.register(WebGuestConfiguration.class);
		return new DispatcherServlet(context);
	}

	@Bean(name = DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME)
	public ServletRegistrationBean guestServletRegistrationBean(DispatcherServlet dispatcherServlet) {
		ServletRegistrationBean registration = new ServletRegistrationBean(dispatcherServlet);
		registration.setName(GUEST_SERVLET_NAME);
		registration.setLoadOnStartup(1);
		registration.addUrlMappings(GUEST_SERVLET_PATH + "/*");
		return registration;
	}

	@Bean
	public DispatcherServlet adminDispatcherServlet() {
		AnnotationConfigEmbeddedWebApplicationContext context = new AnnotationConfigEmbeddedWebApplicationContext();
		context.setResourceLoader(getResourceLoader());
		context.register(WebAdminConfiguration.class);
		return new DispatcherServlet(context);
	}

	@Bean
	public ServletRegistrationBean adminServletRegistrationBean() {
		ServletRegistrationBean registration = new ServletRegistrationBean(adminDispatcherServlet());
		registration.setName(ADMIN_SERVLET_NAME);
		registration.setLoadOnStartup(2);
		registration.addUrlMappings(ADMIN_SERVLET_PATH + "/*");
		return registration;
	}

	@Bean
	public RequestContextListener requestContextListener() {
		return new RequestContextListener();
	}
}
