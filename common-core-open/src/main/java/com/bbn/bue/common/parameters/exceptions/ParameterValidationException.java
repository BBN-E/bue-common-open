package com.bbn.bue.common.parameters.exceptions;


public class ParameterValidationException extends ParameterException {
	public ParameterValidationException(String param, String value,
		Throwable cause)
	{
		super(String.format(
			"For parameter %s with value %s, type conversion was successful, but constraint validation failed.",
				param, value), cause);
	}

	public ParameterValidationException(String param, String value, String cause) {
		super(String.format(
				"For parameter %s with value %s, type conversion was successful, but constraint validation failed: %s.",
					param, value, cause));
	}

	private static final long serialVersionUID = 1L;
}
