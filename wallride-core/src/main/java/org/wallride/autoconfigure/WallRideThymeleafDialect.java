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
import org.wallride.support.ArticleUtils;
import org.wallride.support.CategoryUtils;
import org.wallride.support.PageUtils;
import org.wallride.support.PostUtils;
import org.wallride.web.support.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WallRideThymeleafDialect extends AbstractDialect implements IExpressionEnhancingDialect {

	private PostUtils postUtils;

	private ArticleUtils articleUtils;

	private PageUtils pageUtils;

	private CategoryUtils categoryUtils;

	private WallRideProperties wallRideProperties;

	public PostUtils getPostUtils() {
		return postUtils;
	}

	public void setPostUtils(PostUtils postUtils) {
		this.postUtils = postUtils;
	}

	public ArticleUtils getArticleUtils() {
		return articleUtils;
	}

	public void setArticleUtils(ArticleUtils articleUtils) {
		this.articleUtils = articleUtils;
	}

	public PageUtils getPageUtils() {
		return pageUtils;
	}

	public void setPageUtils(PageUtils pageUtils) {
		this.pageUtils = pageUtils;
	}

	public CategoryUtils getCategoryUtils() {
		return categoryUtils;
	}

	public void setCategoryUtils(CategoryUtils categoryUtils) {
		this.categoryUtils = categoryUtils;
	}

	public WallRideProperties getWallRideProperties() {
		return wallRideProperties;
	}

	public void setWallRideProperties(WallRideProperties wallRideProperties) {
		this.wallRideProperties = wallRideProperties;
	}

	@Override
	public Map<String, Object> getAdditionalExpressionObjects(IProcessingContext processingContext) {
		Map<String, Object> objects = new HashMap<>();
		objects.put("posts", createPosts(processingContext));
		objects.put("articles", createArticles(processingContext));
		objects.put("pages", createPages(processingContext));
		objects.put("categories", createCategories(processingContext));
		objects.put("medias", createMedias(processingContext));
		objects.put("users", createUsers(processingContext));
		objects.put("devices", createDevices(processingContext));
		return Collections.unmodifiableMap(objects);
	}

	protected Posts createPosts(IProcessingContext processingContext) {
		return new Posts(processingContext, postUtils, wallRideProperties);
	}

	protected Articles createArticles(IProcessingContext processingContext) {
		return new Articles(processingContext, articleUtils);
	}

	protected Pages createPages(IProcessingContext processingContext) {
		return new Pages(processingContext, pageUtils);
	}

	protected Categories createCategories(IProcessingContext processingContext) {
		return new Categories(processingContext, categoryUtils);
	}

	protected Medias createMedias(IProcessingContext processingContext) {
		return new Medias(processingContext, wallRideProperties);
	}

	protected Users createUsers(IProcessingContext processingContext) {
		return new Users(processingContext, wallRideProperties);
	}

	protected Devices createDevices(IProcessingContext processingContext) {
		return new Devices(processingContext, new LiteDeviceResolver());
	}

	@Override
	public String getPrefix() {
		return null;
	}
}
