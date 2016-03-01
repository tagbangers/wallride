package org.wallride.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;
import org.wallride.web.WebAdminConfiguration;
import org.wallride.web.WebGuestConfiguration;
import org.wallride.web.support.ExtendedUrlRewriteFilter;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

@Configuration
public class ServletConfiguration implements ResourceLoaderAware {

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
	@ConditionalOnMissingBean(name = "adminServletRegistrationBean")
	public ServletRegistrationBean adminServletRegistrationBean() {
		DispatcherServlet dispatcherServlet = new DispatcherServlet(createAdminServletContext());
		ServletRegistrationBean registration = new ServletRegistrationBean(dispatcherServlet);
		registration.setName(ADMIN_SERVLET_NAME);
		registration.setLoadOnStartup(1);
		registration.addUrlMappings(ADMIN_SERVLET_PATH + "/*");
		return registration;
	}

	@Bean
	@ConditionalOnMissingBean(name = "guestServletRegistrationBean")
	public ServletRegistrationBean guestServletRegistrationBean() {
		DispatcherServlet dispatcherServlet = new DispatcherServlet(createGuestServletContext());
		ServletRegistrationBean registration = new ServletRegistrationBean(dispatcherServlet);
//		registration.setName(DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME);
		registration.setName(GUEST_SERVLET_NAME);
		registration.setLoadOnStartup(2);
		registration.addUrlMappings(GUEST_SERVLET_PATH + "/*");
		return registration;
	}

	protected WebApplicationContext createAdminServletContext() {
		AnnotationConfigEmbeddedWebApplicationContext context = new AnnotationConfigEmbeddedWebApplicationContext();
		context.setResourceLoader(getResourceLoader());
		context.register(WebAdminConfiguration.class);
		return context;
	}

	protected WebApplicationContext createGuestServletContext() {
		AnnotationConfigEmbeddedWebApplicationContext context = new AnnotationConfigEmbeddedWebApplicationContext();
		context.setResourceLoader(getResourceLoader());
		context.register(WebGuestConfiguration.class);
		return context;
	}

	@Bean
	public RequestContextListener requestContextListener() {
		return new RequestContextListener();
	}
}
