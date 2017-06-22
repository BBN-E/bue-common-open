package com.bbn.bue.common.evaluation;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * The information needed to calculate precision, recall, and F-measure.
 */
public abstract class FMeasureInfo {

  public abstract double precision();

  public abstract double recall();

  public final double F1() {
    return F(1.0f);
  }

  public final double F(float beta) {
    checkArgument(beta > 0.0);
    final double recall = recall();
    final double precision = precision();

    if (precision + recall > 0.0) {
      return (1.0 + beta * beta)
          * precision * recall
          / (beta * beta * precision + recall);
    } else {
      return 0.0;
    }
  }

  public static FMeasureInfo aggregateByMacroPR(
      List<FMeasureInfo> corefAgreementsLR) {
    float precisionTotal = 0.0f;
    float recallTotal = 0.0f;

    for (final FMeasureInfo info : corefAgreementsLR) {
      precisionTotal += (float) info.precision();
      recallTotal += (float) info.recall();
    }

    final int count = corefAgreementsLR.size();
    if (count == 0) {
      return new PrecisionRecallPair(0.0f, 0.0f);
    } else {
      return new PrecisionRecallPair(precisionTotal / count, recallTotal / count);
    }
  }

}
