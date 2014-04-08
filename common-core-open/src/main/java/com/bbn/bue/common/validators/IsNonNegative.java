package com.bbn.bue.common.validators;

public class IsNonNegative<T extends Number> implements Validator<T> {
	@Override
	public void validate(T n) throws ValidationException {
		if (n.doubleValue() < 0.0d) {
			throw new ValidationException(String.format(
				"%s is not non-negative", n));
		}
	}
}
