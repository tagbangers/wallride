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

public class WallRideThymeleafDialect extends AbstractDialect implements IExpressionObjectDialect {

	public static final String NAME = "WallRide";

	private WallRideExpressionObjectFactory wallRideExpressionObjectFactory;

	protected WallRideThymeleafDialect(WallRideExpressionObjectFactory wallRideExpressionObjectFactory) {
		super(NAME);
		this.wallRideExpressionObjectFactory = wallRideExpressionObjectFactory;
	}

	@Override
	public IExpressionObjectFactory getExpressionObjectFactory() {
		return wallRideExpressionObjectFactory;
	}
}
