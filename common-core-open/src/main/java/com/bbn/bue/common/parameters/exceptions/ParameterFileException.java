package com.bbn.bue.common.parameters.exceptions;

import com.google.common.annotations.Beta;


@Beta
public class ParameterFileException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ParameterFileException(String msg) {
    super(msg);
  }
}
