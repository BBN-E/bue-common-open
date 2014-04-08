package com.bbn.bue.common.parameters.exceptions;
import java.io.File;

public class ParseFailureException extends ParameterFileException {
	private static final long serialVersionUID = 1L;
	
	public ParseFailureException(final String error, final String line, 
		final File filename, final int lineNumber) 
	{
		super(String.format("%s: Line %d of %s: %s", error, lineNumber, filename, line));
	}	
}