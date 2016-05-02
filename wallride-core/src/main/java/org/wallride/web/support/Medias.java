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

package org.wallride.web.support;

import org.thymeleaf.context.IExpressionContext;
import org.wallride.autoconfigure.WallRideProperties;
import org.wallride.domain.Media;

public class Medias {

	private IExpressionContext context;

	private WallRideProperties wallRideProperties;

	public Medias(IExpressionContext context, WallRideProperties wallRideProperties) {
		this.context = context;
		this.wallRideProperties = wallRideProperties;
	}

	public String link(Media media) {
		return link(media.getId());
	}

	public String link(String id) {
		return wallRideProperties.getMediaUrlPrefix() + id;
	}
}
