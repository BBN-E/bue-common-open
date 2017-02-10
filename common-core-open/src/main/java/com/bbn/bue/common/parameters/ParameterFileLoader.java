package com.bbn.bue.common.parameters;

import com.google.common.annotations.Beta;

import java.io.File;
import java.io.IOException;

@Beta
public interface ParameterFileLoader {

  /**
   * Loads a parameter file.
   */
  Parameters load(File f) throws IOException;
}
