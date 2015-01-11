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

import org.wallride.core.domain.Media;
import org.wallride.core.support.WallRideProperties;

import java.io.Serializable;

public class MediaIndexModel implements Serializable {

	private String thumb;

	private String image;

	private String title;

	private String folder;

	public MediaIndexModel(Media media, WallRideProperties wallRideProperties) {
		this.thumb = wallRideProperties.getMediaUrlPrefix() + media.getId() + "?w=100&h=100&m=1";
		this.image = wallRideProperties.getMediaUrlPrefix() + media.getId();
		this.title = media.getOriginalName();
		this.folder = media.getCreatedAt().toString("yyyy/MM");
	}

	public String getThumb() {
		return thumb;
	}

	public String getImage() {
		return image;
	}

	public String getTitle() {
		return title;
	}

	public String getFolder() {
		return folder;
	}
}
