package org.wallride.support;

import org.springframework.format.Formatter;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.text.ParseException;
import java.util.Locale;

public class CodeFormatter implements Formatter<String> {

	@Override
	public String print(String object, Locale locale) {
		return (!object.equals("") ? object : null);
	}

	@Override
	public String parse(String text, Locale locale) throws ParseException {
		if (text == null) {
			return null;
		}
		String value = StringUtils.trimWhitespace(text);
		value = Normalizer.normalize(value, Normalizer.Form.NFKC);
		value = StringUtils.replace(value, " ", "-");
		return value;
	}
}
