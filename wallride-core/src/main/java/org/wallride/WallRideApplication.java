package org.wallride;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.cloud.aws.core.io.s3.PathMatchingSimpleStorageResourcePatternResolver;
import org.springframework.cloud.aws.core.io.s3.SimpleStorageResourceLoader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.StandardServletEnvironment;
import org.wallride.autoconfigure.WallRideProperties;

import javax.swing.*;

public class WallRideApplication extends SpringApplication {

	public WallRideApplication(Object... sources) {
		super(sources);
		setApplicationContextClass(AnnotationConfigEmbeddedWebApplicationContext.class);
		setEnvironment(createEnvironment());
		setResourceLoader(createResourceLoader());
	}

	public static ConfigurableApplicationContext run(Object source, String... args) {
		return run(new Object[] { DefaultSource.class, source }, args);
	}

	public static ConfigurableApplicationContext run(Object[] sources, String[] args) {
		return new WallRideApplication(sources).run(args);
	}

	@Override
	public ConfigurableApplicationContext run(String... args) {
		return super.run(args);
	}

	public static ConfigurableEnvironment createEnvironment() {
		StandardEnvironment environment = new StandardEnvironment();

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

		return environment;
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

	@EnableAutoConfiguration(exclude = {
			DispatcherServletAutoConfiguration.class,
			WebMvcAutoConfiguration.class,
			SpringDataWebAutoConfiguration.class,
	})
	public static class DefaultSource {}
}
