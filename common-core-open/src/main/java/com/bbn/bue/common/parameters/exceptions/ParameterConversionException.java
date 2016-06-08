package com.bbn.bue.common.parameters.exceptions;

public class ParameterConversionException extends ParameterException {

  public ParameterConversionException(String param, String value,
      Throwable cause, String expectation) {
    super(String.format("For parameter %s, expected %s but got %s.",
        param, expectation, value), cause);
  }

  public ParameterConversionException(String param, String value,
      String expectation) {
    super(String.format("For parameter %s, expected %s but got %s.",
        param, expectation, value));
  }

  private static final long serialVersionUID = 1L;
}
