package com.bbn.bue.common.evaluation;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class FMeasureInfo {

  public abstract float precision();

  public abstract float recall();

  public final float F1() {
    return F(1.0f);
  }

  public final float F(float beta) {
    checkArgument(beta > 0.0);
    final float recall = recall();
    final float precision = precision();

    if (precision + recall > 0.0) {
      return (1.0f + beta * beta)
          * precision * recall
          / (beta * beta * precision + recall);
    } else {
      return 0.0f;
    }
  }

  public static FMeasureInfo aggregateByMacroPR(
      List<FMeasureInfo> corefAgreementsLR) {
    float precisionTotal = 0.0f;
    float recallTotal = 0.0f;

    for (final FMeasureInfo info : corefAgreementsLR) {
      precisionTotal += info.precision();
      recallTotal += info.recall();
    }

    final int count = corefAgreementsLR.size();
    if (count == 0) {
      return new PrecisionRecallPair(0.0f, 0.0f);
    } else {
      return new PrecisionRecallPair(precisionTotal / count, recallTotal / count);
    }
  }

}
