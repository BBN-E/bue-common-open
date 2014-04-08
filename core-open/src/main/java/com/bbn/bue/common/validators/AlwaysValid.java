package com.bbn.bue.common.validators;

public final class AlwaysValid<T> implements Validator<T> {
	public AlwaysValid() {}
	
	/**
	 * This is a no-op. It will never throw an exception.
	 */
	@Override
	public void validate(T arg) { }
}
