package com.bbn.bue.common.serialization.jackson.mixins;

import com.bbn.bue.common.strings.offsets.Offset;
import com.bbn.bue.common.strings.offsets.OffsetRange;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class OffsetRangeMixin<T extends Offset<T>> {
  @JsonCreator
  public static <T extends Offset<T>> OffsetRange<T> fromInclusiveEndpoints(@JsonProperty("start") T start,
      @JsonProperty("end") T end) {
    throw new UnsupportedOperationException("Not meant to be executed");
  }

  abstract @JsonProperty("start") T startInclusive();
  abstract @JsonProperty("end") T endInclusive();
}
