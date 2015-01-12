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

package org.wallride.core.domain;

import org.hibernate.annotations.common.util.StringHelper;
import org.hibernate.search.SearchException;
import org.hibernate.search.bridge.TwoWayStringBridge;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;

public class LocalDateTimeBridge implements TwoWayStringBridge {

	@Override
	public Object stringToObject(String stringValue) {
		if (StringHelper.isEmpty(stringValue)) {
			return null;
		}
		try {
			return LocalDateTime.parse(stringValue, DateTimeFormat.forPattern("yyyyMMddHHmmssSSS"));
		}
		catch (Exception e) {
			throw new SearchException( "Unable to parse into date: " + stringValue, e );
		}
	}

	@Override
	public String objectToString(Object object) {
		if (object == null) {
			return null;
		}
		LocalDateTime date = (LocalDateTime) object;
		return date.toString("yyyyMMddHHmmssSSS");
	}
}
