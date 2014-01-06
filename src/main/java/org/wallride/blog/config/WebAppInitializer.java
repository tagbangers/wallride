package org.wallride.blog.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
import org.wallride.core.support.AmazonS3Resource;
import org.wallride.core.support.AmazonS3ResourceLoader;

import javax.servlet.ServletRegistration.Dynamic;
import java.net.MalformedURLException;
import java.net.URL;

@Order(3)
public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

	private AmazonS3Client amazonS3Client;

	public WebAppInitializer() {
//		System.setProperty("jgroups.s3.access_key", environment.getRequiredProperty("amazon.s3.accesskey"));
//		System.setProperty("jgroups.s3.secret_access_key", environment.getRequiredProperty("amazon.s3.secretkey"));

		final String s3AccessKey = System.getProperty("s3.accessKey");
		final String s3SecretKey = System.getProperty("s3.secretKey");
		BasicAWSCredentials basicAWSCredentials = null;

		if (s3AccessKey != null && s3SecretKey != null) {
			ClientConfiguration configuration = new ClientConfiguration();
			configuration.setMaxConnections(1000);
//			configuration.setSocketBufferSizeHints(10 * 1024 * 1024, 10 * 1024 * 1024);
			amazonS3Client = new AmazonS3Client(new BasicAWSCredentials(s3AccessKey, s3SecretKey), configuration);
		}
	}

	@Override
	protected WebApplicationContext createServletApplicationContext() {
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
					return new AmazonS3Resource(amazonS3Client, bucketName, key);
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
		Class<?>[] servletConfigClasses = this.getServletConfigClasses();
		Assert.notEmpty(servletConfigClasses,
				"getServletConfigClasses() did not return any configuration classes");

		servletAppContext.register(servletConfigClasses);
		return servletAppContext;
	}

	@Override
	protected Class<?>[] getRootConfigClasses() {
		return null;
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class[] {
				BlogConfig.class,
				CacheConfig.class,
				WebConfig.class,
		};
	}
	
	@Override
	protected String getServletName() {
		return "blogServlet";
	}
	
	@Override
	protected String[] getServletMappings() {
		return new String[] { "/*" };
	}
	
//	@Override
//	protected Filter[] getServletFilters() {
//		CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
//		characterEncodingFilter.setEncoding("UTF-8");
//		characterEncodingFilter.setForceEncoding(true);
//		return new Filter[] { characterEncodingFilter};
//	}
	
	@Override
	protected void customizeRegistration(Dynamic registration) {
		registration.setLoadOnStartup(2);
	}
}
