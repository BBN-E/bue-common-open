package com.bbn.bue.common.serialization.jackson.mixins;

import com.bbn.bue.common.evaluation.FMeasureCounts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class FMeasureCountsMixin {

  @JsonCreator
  public static FMeasureCounts from(@JsonProperty("truePositives") final float truePositives,
      @JsonProperty("falsePositives") final float falsePositives,
      @JsonProperty("falseNegatives") final float falseNegatives) {
    return null;
  }

  @JsonProperty("truePositives")
  public abstract float truePositives();

  @JsonProperty("falsePositives")
  public abstract float falsePositives();

  @JsonProperty("falseNegatives")
  public abstract float falseNegatives();
}
