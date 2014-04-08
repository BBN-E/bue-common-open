package com.bbn.bue.common.parameters;

import java.util.Map;
import java.io.File;
import java.io.IOException;

import com.google.common.annotations.Beta;

@Beta
public interface ParameterFileLoader {
	/**
	 * Loads a parameter file.
	 */
	public Map<String, String> load(File f) throws IOException;
}
