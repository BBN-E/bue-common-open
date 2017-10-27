package com.bbn.nlp.indri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import lemurproject.indri.QueryEnvironment;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utilities for working with Indri.
 */
public final class Indri {

  private static final Logger log = LoggerFactory.getLogger(Indri.class);

  private Indri() {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets an {@link com.bbn.nlp.indri.Indri.IndriQueryerBuilder} set up to use the specified
   * directory as an Indri index.
   */
  public static IndriQueryerBuilder queryerForIndex(File indexDir) {
    final IndriQueryerBuilder builder = new IndriQueryerBuilder();
    builder.addIndex(indexDir);
    return builder;
  }

  public static class IndriQueryerBuilder {

    private final QueryEnvironment queryEnvironment = new QueryEnvironment();
    private String docIdField = "docno";

    private IndriQueryerBuilder() {
    }

    /**
     * Tells the Indri queryer to use the specified index directory. Multiple
     * indices may be specified.
     */
    public IndriQueryerBuilder addIndex(File indexDir) {
      checkArgument(indexDir.isDirectory(), "Indri index directory %s either does not "
          + "exist or is not a directory", indexDir);
      try {
        queryEnvironment.addIndex(indexDir.getAbsolutePath());
        log.info("Using Indri index {}", indexDir);
      } catch (Exception e) {
        throw new IndriException(e);
      }
      return this;
    }

    /**
     * Specifies what metadata field to use for document IDs.  Default is "docno", which is
     * appropriate in most cases.
     */
    public IndriQueryerBuilder withDocIdField(String docIdField) {
      this.docIdField = checkNotNull(docIdField);
      return this;
    }

    public IndriQueryer build() {
      return new DefaultIndriQueryer(queryEnvironment, docIdField);
    }
  }
}
