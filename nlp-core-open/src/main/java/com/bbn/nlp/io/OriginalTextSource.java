package com.bbn.nlp.io;

import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Optional;

import java.io.IOException;

/**
 * Interface for something which can provide the original text for a document.
 *
 *
 * This should be merged into the newer {@link com.bbn.bue.common.files.KeyValueSource}
 * code.
 *
 * @author Ryan Gabbard
 */
public interface OriginalTextSource {

  /**
   * Returns the original document text for the specified document ID, if available. If it is not,
   * returns {@link com.google.common.base.Optional#absent()}.
   */
  public Optional<String> getOriginalText(Symbol docID) throws IOException;
}
