package com.bbn.bue.common.parameters.serifstyle;

import com.bbn.bue.common.parameters.exceptions.ParameterFileException;

import java.util.Map;

public class InterpolationException extends ParameterFileException {

  public static final long serialVersionUID = 1L;

  public InterpolationException(String param, Map<String, String> params) {
    super(String.format("Could not interpolate for %s. Available parameters are %s", param,
        params.keySet()));
  }
}
