package com.bbn.bue.common.serialization.jackson.mixins;

import com.bbn.bue.common.symbols.Symbol;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Allows Jackson serialization of symbols without modifying Symbol class.
 *
 * @author rgabbard
 */
public abstract class SymbolMixin {

  @JsonCreator
  public static synchronized Symbol from(@JsonProperty("string") final String string) {
    return null;
  }

  @Override
  public abstract
  @JsonProperty("string")
  String toString();
}
