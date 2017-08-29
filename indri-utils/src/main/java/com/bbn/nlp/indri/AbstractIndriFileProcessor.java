package com.bbn.nlp.indri;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * A convenience class for implementing {@link com.bbn.nlp.indri.IndriFileProcessor}s which {@link
 * #documentsForFile(java.io.File)} delegate to {@link #documentsForString(String)} by simply
 * reading the whole file into memory.
 */
public abstract class AbstractIndriFileProcessor implements IndriFileProcessor {

  @Override
  public Iterator<String> documentsForFile(File f) throws IOException {
    return documentsForString(Files.asCharSource(f, Charsets.UTF_8).read());
  }
}
