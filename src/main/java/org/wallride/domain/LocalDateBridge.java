package org.wallride.domain;

import org.hibernate.annotations.common.util.StringHelper;
import org.hibernate.search.SearchException;
import org.hibernate.search.bridge.TwoWayStringBridge;
import org.joda.time.LocalDate;

public class LocalDateBridge implements TwoWayStringBridge {

	@Override
	public Object stringToObject(String stringValue) {
		if (StringHelper.isEmpty(stringValue)) {
			return null;
		}
		try {
			return LocalDate.parse(stringValue);
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
		LocalDate date = (LocalDate) object;
		return date.toString();
	}
}
