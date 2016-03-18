package com.bbn.nlp.corenlp;

import com.google.common.annotations.Beta;

/**
 * Holds data stored on an {@link CoreNLPParseNode}
 *
 * Cannot be instantiated because a CoreNLPParseNode does not store an auxiliary data. This is a
 * place holder class.
 */
@Beta
public final class CoreNLPParseNodeData {

  private CoreNLPParseNodeData() {
    throw new UnsupportedOperationException();
  }
}
