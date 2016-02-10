package com.bbn.bue.common.evaluation;

import static com.google.common.base.Preconditions.checkArgument;

public final class PrecisionRecallPair extends FMeasureInfo {

  public PrecisionRecallPair(double precision, double recall) {
    checkArgument(precision >= 0.0);
    checkArgument(recall >= 0.0);

    this.precision = precision;
    this.recall = recall;
  }

  public double precision() {
    return precision;
  }

  public double recall() {
    return recall;
  }

  private final double precision;
  private final double recall;
}
