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
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.expression.IExpressionObjectFactory;
import org.wallride.support.ArticleUtils;
import org.wallride.support.CategoryUtils;
import org.wallride.support.PageUtils;
import org.wallride.support.PostUtils;
import org.wallride.support.TagUtils;
import org.wallride.web.support.Articles;
import org.wallride.web.support.Categories;
import org.wallride.web.support.Devices;
import org.wallride.web.support.Medias;
import org.wallride.web.support.Pages;
import org.wallride.web.support.Posts;
import org.wallride.web.support.Tags;
import org.wallride.web.support.Users;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class WallRideExpressionObjectFactory implements IExpressionObjectFactory {

	public static final String POSTS_EXPRESSION_OBJECT_NAME = "posts";
	public static final String ARTICLES_EXPRESSION_OBJECT_NAME = "articles";
	public static final String PAGES_EXPRESSION_OBJECT_NAME = "pages";
	public static final String CATEGORIES_EXPRESSION_OBJECT_NAME = "categories";
	public static final String TAGS_EXPRESSION_OBJECT_NAME = "tags";
	public static final String MEDIAS_EXPRESSION_OBJECT_NAME = "medias";
	public static final String USERS_EXPRESSION_OBJECT_NAME = "users";
	public static final String DEVICES_EXPRESSION_OBJECT_NAME = "devices";

	protected static final Set<String> ALL_EXPRESSION_OBJECT_NAMES =
			Collections.unmodifiableSet(new LinkedHashSet<>(java.util.Arrays.asList(
					new String[] {
							POSTS_EXPRESSION_OBJECT_NAME,
							ARTICLES_EXPRESSION_OBJECT_NAME,
							PAGES_EXPRESSION_OBJECT_NAME,
							CATEGORIES_EXPRESSION_OBJECT_NAME,
							TAGS_EXPRESSION_OBJECT_NAME,
							MEDIAS_EXPRESSION_OBJECT_NAME,
							USERS_EXPRESSION_OBJECT_NAME,
							DEVICES_EXPRESSION_OBJECT_NAME,
					}
			)));

	private PostUtils postUtils;

	private ArticleUtils articleUtils;

	private PageUtils pageUtils;

	private CategoryUtils categoryUtils;

	private TagUtils tagUtils;

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

	public TagUtils getTagUtils() {
		return tagUtils;
	}

	public void setTagUtils(TagUtils tagUtils) {
		this.tagUtils = tagUtils;
	}

	public WallRideProperties getWallRideProperties() {
		return wallRideProperties;
	}

	public void setWallRideProperties(WallRideProperties wallRideProperties) {
		this.wallRideProperties = wallRideProperties;
	}

	@Override
	public Set<String> getAllExpressionObjectNames() {
		return ALL_EXPRESSION_OBJECT_NAMES;
	}

	@Override
	public boolean isCacheable(String expressionObjectName) {
		return true;
	}

	@Override
	public Object buildObject(IExpressionContext context, String expressionObjectName) {
		switch (expressionObjectName) {
			case POSTS_EXPRESSION_OBJECT_NAME:
				return createPosts(context);
			case ARTICLES_EXPRESSION_OBJECT_NAME:
				return createArticles(context);
			case PAGES_EXPRESSION_OBJECT_NAME:
				return createPages(context);
			case CATEGORIES_EXPRESSION_OBJECT_NAME:
				return createCategories(context);
			case TAGS_EXPRESSION_OBJECT_NAME:
				return createTags(context);
			case MEDIAS_EXPRESSION_OBJECT_NAME:
				return createMedias(context);
			case USERS_EXPRESSION_OBJECT_NAME:
				return createUsers(context);
			case DEVICES_EXPRESSION_OBJECT_NAME:
				return createDevices(context);
			default:
				return null;
		}
	}

	protected Posts createPosts(IExpressionContext context) {
		return new Posts(context, postUtils, wallRideProperties);
	}

	protected Articles createArticles(IExpressionContext context) {
		return new Articles(context, articleUtils);
	}

	protected Pages createPages(IExpressionContext context) {
		return new Pages(context, pageUtils);
	}

	protected Categories createCategories(IExpressionContext context) {
		return new Categories(context, categoryUtils);
	}

	protected Tags createTags(IExpressionContext context) {
		return new Tags(context, tagUtils);
	}

	protected Medias createMedias(IExpressionContext context) {
		return new Medias(context, wallRideProperties);
	}

	protected Users createUsers(IExpressionContext context) {
		return new Users(context, wallRideProperties);
	}

	protected Devices createDevices(IExpressionContext context) {
		return new Devices(context, new LiteDeviceResolver());
	}
}
