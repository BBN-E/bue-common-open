package com.bbn.bue.common.serialization.jackson.mixins;

import com.bbn.bue.common.evaluation.FMeasureCounts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class FMeasureCountsMixin {

  @JsonCreator
  public static FMeasureCounts fromFPFNKeyCountSysCount
      (@JsonProperty("falsePositives") final double falsePositives,
          @JsonProperty("falseNegatives") final double falseNegatives,
          @JsonProperty("keyCount") double keyCount,
          @JsonProperty("sysCount") double sysCount) {
    return null;
  }

  @JsonProperty("falsePositives")
  public abstract float falsePositives();

  @JsonProperty("falseNegatives")
  public abstract float falseNegatives();

  @JsonProperty("keyCount")
  public abstract double numItemsInKey();

  @JsonProperty("sysCount")
  public abstract double numPredicted();
}
