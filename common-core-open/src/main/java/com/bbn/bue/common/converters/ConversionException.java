package com.bbn.bue.common.converters;

public class ConversionException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ConversionException(String msg) {
    super(msg);
  }

  public ConversionException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public ConversionException(Throwable cause) {
    super(cause);
  }
}
