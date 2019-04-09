package org.wallride.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletRegistrationBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.boot.web.servlet.filter.OrderedFormContentFilter;
import org.springframework.boot.web.servlet.filter.OrderedHiddenHttpMethodFilter;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.filter.FormContentFilter;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;
import org.wallride.service.BlogService;
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
	@ConditionalOnMissingBean(FormContentFilter.class)
	public OrderedFormContentFilter httpPutFormContentFilter() {
		return new OrderedFormContentFilter();
	}

	@Bean
	@DependsOn("cacheManager")
	@ConditionalOnMissingBean(UrlRewriteFilter.class)
	public UrlRewriteFilter urlRewriteFilter(BlogService blogService) {
		return new ExtendedUrlRewriteFilter(blogService);
	}

	@Bean
	public FilterRegistrationBean urlRewriteFilterRegistration() {
		DelegatingFilterProxy proxy = new DelegatingFilterProxy("urlRewriteFilter");
		proxy.setTargetFilterLifecycle(true);

		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setName("urlRewriteFilter");
		registration.setFilter(proxy);
		registration.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
		registration.addUrlPatterns("/*");
		registration.setOrder(0);
		return registration;
	}

	@Bean(name = DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
	public DispatcherServlet guestDispatcherServlet() {
		AnnotationConfigServletWebServerApplicationContext context = new AnnotationConfigServletWebServerApplicationContext();
		context.setResourceLoader(getResourceLoader());
		context.register(WebGuestConfiguration.class);
		WallRideDispatcherServlet dispatcherServlet = new WallRideDispatcherServlet(context);
		dispatcherServlet.setDetectParentHandlerMappings(true);
		return dispatcherServlet;
	}

	@Bean(name = DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME)
	public DispatcherServletRegistrationBean guestServletRegistrationBean(DispatcherServlet dispatcherServlet) {
		DispatcherServletRegistrationBean registration = new DispatcherServletRegistrationBean(dispatcherServlet, GUEST_SERVLET_PATH + "/*");
		registration.setName(GUEST_SERVLET_NAME);
		registration.setLoadOnStartup(1);
		return registration;
	}

	@Bean
	public DispatcherServlet adminDispatcherServlet() {
		AnnotationConfigServletWebServerApplicationContext context = new AnnotationConfigServletWebServerApplicationContext();
		context.setResourceLoader(getResourceLoader());
		context.register(WebAdminConfiguration.class);
		WallRideDispatcherServlet dispatcherServlet = new WallRideDispatcherServlet(context);
		return dispatcherServlet;
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
