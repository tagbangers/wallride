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

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.context.IProcessingContext;
import org.wallride.autoconfigure.WallRideProperties;
import org.wallride.domain.Blog;
import org.wallride.domain.User;

import java.util.HashMap;
import java.util.Map;

public class Users {

	private IProcessingContext processingContext;

	private WallRideProperties wallRideProperties;

	public Users(IProcessingContext processingContext, WallRideProperties wallRideProperties) {
		this.processingContext = processingContext;
		this.wallRideProperties = wallRideProperties;
	}

	public String link(User user) {
		UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath();
		return path(builder, user, true);
	}

	public String link(User user, boolean encode) {
		UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath();
		return path(builder, user, encode);
	}

	public String path(User user) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("");
		return path(builder, user, true);
	}

	public String path(User user, boolean encode) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("");
		return path(builder, user, encode);
	}

	private String path(UriComponentsBuilder builder, User user, boolean encode) {
		Map<String, Object> params = new HashMap<>();
		builder.path("/author/{code}");
		params.put("code", user.getLoginId());

		UriComponents components = builder.buildAndExpand(params);
		if (encode) {
			components = components.encode();
		}
		return components.toUriString();
	}

	public String title(User user) {
		Blog blog = (Blog) processingContext.getContext().getVariables().get("BLOG");
		return String.format("%s | %s",
				user.getNickname(),
				blog.getTitle(processingContext.getContext().getLocale().getLanguage()));
	}
}
