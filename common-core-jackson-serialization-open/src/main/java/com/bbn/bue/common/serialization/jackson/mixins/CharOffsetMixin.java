package com.bbn.bue.common.serialization.jackson.mixins;

import com.bbn.bue.common.strings.offsets.CharOffset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Allow Jackson serialization of CharOffsets without modifying CharOffset class
 *
 * @author rgabbard
 */
public abstract class CharOffsetMixin {

  @JsonCreator
  public static CharOffset asCharOffset(@JsonProperty("value") int value) {
    throw new UnsupportedOperationException();
  }

  @JsonProperty("value")
  public abstract int value();
}


