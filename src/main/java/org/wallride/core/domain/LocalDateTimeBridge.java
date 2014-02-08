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
