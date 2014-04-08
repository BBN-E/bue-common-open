package com.bbn.bue.common.parameters.exceptions;

import com.google.common.base.Joiner;


public class InvalidEnumeratedPropertyException extends ParameterException {
	private static final long serialVersionUID = 1L;

	public InvalidEnumeratedPropertyException(String param, String value, Iterable<String> possibleValues) {
		super(String.format("For parameter %s, got %s but needed one of %s", param, value,
			Joiner.on(",").join(possibleValues)));
	}
}