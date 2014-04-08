package com.bbn.bue.common.parameters.exceptions;

public class MissingRequiredParameter extends ParameterException {
	private static final long serialVersionUID = 1L;

	public MissingRequiredParameter(String param) {
		super(String.format("Missing required parameter %s", param)); 
	}
}