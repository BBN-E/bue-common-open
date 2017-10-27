package com.bbn.nlp.indri;

import com.bbn.bue.common.parameters.Parameters;

import com.google.common.base.Charsets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An {@link com.bbn.nlp.indri.IndriFileProcessor} for text files where each file is a
 * single document and there is no special formatting.
 */
public final class OneDocPerFileProcessor implements IndriFileProcessor {

  private OneDocPerFileProcessor() {
  }

  public static OneDocPerFileProcessor create() {
    return new OneDocPerFileProcessor();
  }

  public static OneDocPerFileProcessor fromParameters(Parameters params) {
    return new OneDocPerFileProcessor();
  }

  @Override
  public Iterator<String> documentsForFile(File f) throws IOException {
    List<String> strings = new ArrayList<String>();
    String docid = f.getName();
    String raw_text = com.google.common.io.Files.asCharSource(f, Charsets.UTF_8).read();
    String trec_text =
        String.format("<DOC><DOCNO>%s</DOCNO><TEXT>%s</TEXT></DOC>", docid, raw_text);
    strings.add(trec_text);
    return strings.iterator();
  }

  @Override
  public Iterator<String> documentsForString(String s) {
    throw new java.lang.UnsupportedOperationException(
        "OneDocPerFileProcessor does not implement documentsForString()!  Use documentsForFile() instead.");
  }
}
