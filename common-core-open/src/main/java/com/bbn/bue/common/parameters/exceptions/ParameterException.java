package com.bbn.bue.common.parameters.exceptions;

import com.google.common.annotations.Beta;


@Beta
public class ParameterException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ParameterException(String msg) { super(msg); }
	public ParameterException(String msg, Throwable cause) { super(msg, cause); }
}