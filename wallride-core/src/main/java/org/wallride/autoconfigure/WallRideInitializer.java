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
import java.io.*;
import java.net.URL;
import java.util.Properties;

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
			//try to get config-File with wallride.home parameter under webroot
			String configFileHome = getConfigFileHome(event);
			if (configFileHome!=null) {
				home = configFileHome;
			} else {
				throw new IllegalStateException(WallRideProperties.HOME_PROPERTY + " is empty");
			}
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

	private static String getConfigFileHome(ApplicationStartingEvent event) {

		File configFile = getConfigFileFromWebroot(event);
		if (configFile!=null) {
			Properties properties = getProperties(configFile);
			if (properties.getProperty(WallRideProperties.HOME_PROPERTY) != null) {
				String home = properties.getProperty(WallRideProperties.HOME_PROPERTY);
				if (!home.startsWith("file:")) {
					home = "file:"+home;
				}
				return home;
			} else {
				throw new IllegalStateException(WallRideProperties.HOME_PROPERTY + " not found in config file " + configFile.getAbsolutePath());
			}
		}

		return null;
	}

	/**
	 * Try to find a config file where the wallride.home parameter is configured
	 * Config file must be placed under the webroot directory and can be named wallride.conf or webroot-name.conf
	 * Example: if webroot is /srv/webapps/myblog the config file can be /srv/webapps/wallride.conf or /srv/webapps/myblog.conf
	 * @param event
	 * @return
	 */
	private static File getConfigFileFromWebroot(ApplicationStartingEvent event) {

		URL resource = event.getClass().getClassLoader().getResource("");

		File classPath = new File(resource.getPath()); // ROOT/WEB-INF/classes/
		File webInfPath = classPath.getParentFile(); // ROOT/WEB-INF/
		if (webInfPath.getName().equalsIgnoreCase("WEB-INF")) {
			File rootPath = webInfPath.getParentFile(); // ROOT/
			File wallrideConfigFile = new File(rootPath.getParentFile(), "wallride.conf");
			if (wallrideConfigFile.exists()) { return wallrideConfigFile; }

			File configFile = new File(rootPath.getParentFile(), rootPath.getName()+".conf");
			if (configFile.exists()) { return configFile; }

		} else {
			//there is no web-inf directory -> webroot can not be determined
		}

		return null;
	}

	private static Properties getProperties(File configFile) {

		Properties confFileProperties = new Properties();
		InputStream is = null;
		try {
			is = new FileInputStream(configFile);
			confFileProperties.load(is);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (is!=null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return confFileProperties;
	}
}