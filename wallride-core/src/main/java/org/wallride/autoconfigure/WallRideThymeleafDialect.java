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

import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;
import org.wallride.support.ArticleUtils;
import org.wallride.support.CategoryUtils;
import org.wallride.support.PageUtils;
import org.wallride.support.PostUtils;

public class WallRideThymeleafDialect extends AbstractDialect implements IExpressionObjectDialect {

	public static final String NAME = "WallRide";

	private PostUtils postUtils;

	private ArticleUtils articleUtils;

	private PageUtils pageUtils;

	private CategoryUtils categoryUtils;

	private WallRideProperties wallRideProperties;

	protected WallRideThymeleafDialect() {
		super(NAME);
	}

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
	public IExpressionObjectFactory getExpressionObjectFactory() {
		WallRideExpressionObjectFactory expressionObjectFactory = new WallRideExpressionObjectFactory();
		expressionObjectFactory.setPostUtils(postUtils);
		expressionObjectFactory.setArticleUtils(articleUtils);
		expressionObjectFactory.setPageUtils(pageUtils);
		expressionObjectFactory.setCategoryUtils(categoryUtils);
		expressionObjectFactory.setWallRideProperties(wallRideProperties);
		return expressionObjectFactory;
	}
}
