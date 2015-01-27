package com.bbn.bue.common.exceptions;

public class NotImplementedException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public NotImplementedException() {
  }

  public NotImplementedException(String msg) {
    super(msg);
  }
}
