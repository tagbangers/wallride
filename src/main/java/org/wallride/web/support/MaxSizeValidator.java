package org.wallride.web.support;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MaxSizeValidator implements ConstraintValidator<MaxSize, String> {

	private int max;

	@Override
	public void initialize(MaxSize constraintAnnotation) {
		max = constraintAnnotation.value();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		//null values are valid
		if (value == null) {
			return true;
		}
		return (value.length() <= max);
	}
}
