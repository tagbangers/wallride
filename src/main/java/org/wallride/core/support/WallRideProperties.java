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
import org.springframework.web.util.UriComponentsBuilder;

@ConfigurationProperties("wallride")
public class WallRideProperties {

	public static final String CONFIG_PATH = "config/";
	public static final String MEDIA_PATH = "media/";

	private String home;
	private String mediaUrlPrefix = "/media/";

	public String getHome() {
		return home;
	}

	public void setHome(String home) {
		this.home = home;
	}

	public String getMediaUrlPrefix() {
		return mediaUrlPrefix;
	}

	public void setMediaUrlPrefix(String mediaUrlPrefix) {
		this.mediaUrlPrefix = mediaUrlPrefix;
	}

	public String getMediaLocation() {
		return UriComponentsBuilder.fromPath(getHome()).path(MEDIA_PATH).buildAndExpand().toUriString();
	}
}
