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
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cloud.aws.core.io.s3.PathMatchingSimpleStorageResourcePatternResolver;
import org.springframework.cloud.aws.core.io.s3.SimpleStorageResourceLoader;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StringUtils;

public class WallRideInitializer implements ApplicationListener<ApplicationStartedEvent> {

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		event.getSpringApplication().setEnvironment(createEnvironment());
		event.getSpringApplication().setResourceLoader(createResourceLoader());
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
}