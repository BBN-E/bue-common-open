package com.bbn.bue.common.parameters;

import com.google.common.annotations.Beta;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Beta
public interface ParameterFileLoader {

  /**
   * Loads a parameter file.
   */
  public Map<String, String> load(File f) throws IOException;
}
