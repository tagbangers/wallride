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

package org.wallride.autoconfigure;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.cloud.aws.core.io.s3.PathMatchingSimpleStorageResourcePatternResolver;
import org.springframework.cloud.aws.core.io.s3.SimpleStorageProtocolResolver;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;

public class WallRideInitializer implements ApplicationListener<ApplicationStartingEvent> {

	/**
	 * @see ConfigFileApplicationListener#DEFAULT_SEARCH_LOCATIONS
	 */
	private static final String DEFAULT_CONFIG_SEARCH_LOCATIONS = "classpath:/,classpath:/config/,file:./,file:./config/";

	@Override
	public void onApplicationEvent(ApplicationStartingEvent event) {
		event.getSpringApplication().setEnvironment(createEnvironment(event));
		event.getSpringApplication().setResourceLoader(createResourceLoader());
	}

	public static ConfigurableEnvironment createEnvironment(ApplicationStartingEvent event) {
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

		event.getSpringApplication().getListeners().stream()
				.filter(listener -> listener.getClass().isAssignableFrom(ConfigFileApplicationListener.class))
				.map(listener -> (ConfigFileApplicationListener) listener)
				.forEach(listener -> listener.setSearchLocations(DEFAULT_CONFIG_SEARCH_LOCATIONS + "," + config));

		return environment;
	}

	public static ResourceLoader createResourceLoader() {
		ClientConfiguration configuration = new ClientConfiguration();
		configuration.setMaxConnections(1000);
		AmazonS3 amazonS3 = new AmazonS3Client(configuration);

		SimpleStorageProtocolResolver protocolResolver = new SimpleStorageProtocolResolver(amazonS3);
		protocolResolver.afterPropertiesSet();
		DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
		resourceLoader.addProtocolResolver(protocolResolver);
		ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver(resourceLoader);
		return new PathMatchingSimpleStorageResourcePatternResolver(amazonS3, resourceResolver);
	}
}