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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.wallride.service.CategoryService;
import org.wallride.service.PageService;
import org.wallride.support.CategoryUtils;
import org.wallride.support.PageUtils;

@Configuration
public class WallRideThymeleafConfiguration extends ThymeleafAutoConfiguration {

	@Autowired
	private WallRideProperties wallRideProperties;

	@Autowired
	private PageService pageService;

	@Autowired
	private CategoryService categoryService;

	@Bean
	public PageUtils pageUtils() {
		return new PageUtils(pageService);
	}

	@Bean
	public CategoryUtils categoryUtils() {
		return new CategoryUtils(categoryService);
	}

	@Bean
	public WallRideThymeleafDialect wallRideThymeleafDialect() {
		WallRideThymeleafDialect dialect = new WallRideThymeleafDialect();
		dialect.setPageUtils(pageUtils());
		dialect.setCategoryUtils(categoryUtils());
		dialect.setWallRideProperties(wallRideProperties);
		return dialect;
	}
}
