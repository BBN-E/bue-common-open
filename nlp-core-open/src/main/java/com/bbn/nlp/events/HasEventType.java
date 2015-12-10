package com.bbn.nlp.events;

import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Function;

public interface HasEventType {

  Symbol eventType();

  enum ExtractFunction implements Function<HasEventType, String> {
    INSTANCE;

    @Override
    public String apply(final HasEventType input) {
      return input.eventType().asString();
    }
  }
}
