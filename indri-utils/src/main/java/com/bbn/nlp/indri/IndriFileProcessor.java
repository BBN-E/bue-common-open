package com.bbn.nlp.indri;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Specifies a way of computing the documents that should be presented to Indri for indexing from
 * either a file or a string.
 */
public interface IndriFileProcessor {

  public Iterator<String> documentsForFile(File f) throws IOException;

  public Iterator<String> documentsForString(String s);
}
