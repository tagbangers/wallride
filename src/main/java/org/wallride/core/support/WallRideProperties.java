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

package org.wallride.core.support;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("wallride")
public class WallRideProperties {

	public static final String HOME_PROPERTY = "wallride.home";
	public static final String CONFIG_LOCATION_PROPERTY = "wallride.config-location";
	public static final String MEDIA_LOCATION_PROPERTY = "wallride.media-location";

	public static final String DEFAULT_CONFIG_PATH_NAME = "config/";
	public static final String DEFAULT_MEDIA_PATH_NAME = "media/";

	private String home;
	private String configLocation;
	private String mediaLocation;
	private String mediaUrlPrefix = "/media/";

	public String getHome() {
		return home;
	}

	public void setHome(String home) {
		this.home = home;
	}

	public String getConfigLocation() {
		return configLocation;
	}

	public void setConfigLocation(String configLocation) {
		this.configLocation = configLocation;
	}

	public String getMediaLocation() {
		return mediaLocation;
	}

	public void setMediaLocation(String mediaLocation) {
		this.mediaLocation = mediaLocation;
	}

	public String getMediaUrlPrefix() {
		return mediaUrlPrefix;
	}

	public void setMediaUrlPrefix(String mediaUrlPrefix) {
		this.mediaUrlPrefix = mediaUrlPrefix;
	}
}
