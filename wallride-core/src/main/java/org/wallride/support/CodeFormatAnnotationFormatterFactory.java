package org.wallride.support;

import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Formatter;
import org.springframework.format.Parser;
import org.springframework.format.Printer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CodeFormatAnnotationFormatterFactory implements AnnotationFormatterFactory<CodeFormat> {

	private static final Set<Class<?>> FIELD_TYPES;

	static {
		Set<Class<?>> fieldTypes = new HashSet<>(1);
		fieldTypes.add(String.class);
		FIELD_TYPES = Collections.unmodifiableSet(fieldTypes);
	}

	@Override
	public Set<Class<?>> getFieldTypes() {
		return FIELD_TYPES;
	}

	@Override
	public Printer<?> getPrinter(CodeFormat annotation, Class<?> fieldType) {
		return getFormatter(annotation, fieldType);
	}

	@Override
	public Parser<?> getParser(CodeFormat annotation, Class<?> fieldType) {
		return getFormatter(annotation, fieldType);
	}

	protected Formatter<String> getFormatter(CodeFormat annotation, Class<?> fieldType) {
		return new CodeFormatter();
	}
}
