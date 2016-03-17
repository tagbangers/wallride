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

package org.wallride.web.controller.admin.media;

import org.wallride.autoconfigure.WallRideProperties;
import org.wallride.domain.Media;

import java.io.Serializable;

public class MediaCreatedModel implements Serializable {

	private String id;

	private String link;

	private String name;

	public MediaCreatedModel(Media media, WallRideProperties wallRideProperties) {
		this.id = media.getId();
		this.link = wallRideProperties.getMediaUrlPrefix() + media.getId();
		this.name = media.getOriginalName();
	}

	public String getId() {
		return id;
	}

	public String getLink() {
		return link;
	}

	public String getName() {
		return name;
	}
}
