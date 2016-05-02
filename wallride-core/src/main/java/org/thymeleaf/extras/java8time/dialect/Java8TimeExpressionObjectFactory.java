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

package org.thymeleaf.extras.java8time.dialect;

import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.expression.IExpressionObjectFactory;
import org.thymeleaf.extras.java8time.expression.Temporals;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author OGAWA, Takeshi
 */
public class Java8TimeExpressionObjectFactory implements IExpressionObjectFactory {

	public static final String EXPRESSION_OBJECT_NAME = "temporals";

	protected static final Set<String> ALL_EXPRESSION_OBJECT_NAMES =
			Collections.unmodifiableSet(new LinkedHashSet<>(java.util.Arrays.asList(
					new String[]{
							EXPRESSION_OBJECT_NAME,
					}
			)));

	@Override
	public Set<String> getAllExpressionObjectNames() {
		return ALL_EXPRESSION_OBJECT_NAMES;
	}

	@Override
	public Object buildObject(IExpressionContext context, String expressionObjectName) {
		if (EXPRESSION_OBJECT_NAME.equals(expressionObjectName)) {
			return new Temporals(context.getLocale());
		}
		return null;
	}

	@Override
	public boolean isCacheable(String expressionObjectName) {
		return true;
	}
}
