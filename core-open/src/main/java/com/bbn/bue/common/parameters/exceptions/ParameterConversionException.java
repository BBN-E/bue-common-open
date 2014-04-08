package com.bbn.bue.common.parameters.exceptions;

public class ParameterConversionException extends ParameterException {
	public ParameterConversionException(String param, String value,
		Throwable cause, String expectation) 
	{
		super(String.format("For parameter %s, expected %s but got string of wrong type %s.", 
				param, value, expectation), cause);
	}

	private static final long serialVersionUID = 1L;	
}
