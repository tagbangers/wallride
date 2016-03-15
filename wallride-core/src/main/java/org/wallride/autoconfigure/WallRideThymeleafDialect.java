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

import org.springframework.mobile.device.LiteDeviceResolver;
import org.thymeleaf.context.IProcessingContext;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionEnhancingDialect;
import org.wallride.support.CategoryUtils;
import org.wallride.support.PageUtils;
import org.wallride.support.PostUtils;
import org.wallride.web.support.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WallRideThymeleafDialect extends AbstractDialect implements IExpressionEnhancingDialect {

	private PostUtils postUtils;

	private PageUtils pageUtils;

	private CategoryUtils categoryUtils;

	private WallRideProperties wallRideProperties;

	public void setPostUtils(PostUtils postUtils) {
		this.postUtils = postUtils;
	}

	public void setPageUtils(PageUtils pageUtils) {
		this.pageUtils = pageUtils;
	}

	public void setCategoryUtils(CategoryUtils categoryUtils) {
		this.categoryUtils = categoryUtils;
	}

	public void setWallRideProperties(WallRideProperties wallRideProperties) {
		this.wallRideProperties = wallRideProperties;
	}

	@Override
	public Map<String, Object> getAdditionalExpressionObjects(IProcessingContext processingContext) {
		Map<String, Object> objects = new HashMap<>();
		objects.put("posts", new Posts(processingContext, postUtils, wallRideProperties));
		objects.put("pages", new Pages(processingContext, pageUtils));
		objects.put("categorys", new Categorys(processingContext, categoryUtils));
		objects.put("medias", new Medias(processingContext, wallRideProperties));
		objects.put("users", new Users(processingContext, wallRideProperties));
		objects.put("devices", new Devices(processingContext, new LiteDeviceResolver()));
		return Collections.unmodifiableMap(objects);
	}

	@Override
	public String getPrefix() {
		return null;
	}
}
