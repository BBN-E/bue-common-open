package com.bbn.bue.common.evaluation;

import com.bbn.bue.common.collections.IterableUtils.ZipPair;
import com.bbn.bue.common.symbols.Symbol;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import static com.bbn.bue.common.collections.IterableUtils.zip;
import static junit.framework.TestCase.assertEquals;

public class SummaryConfusionMatrixTest {

  private static final Symbol LOC = Symbol.from("LOC");
  private static final Symbol ORG = Symbol.from("ORG");
  private static final Symbol PER = Symbol.from("PER");


  @Test
  public void testSummaryConfusionMatrix() {
    final ImmutableList<Symbol> systemAssignments = ImmutableList.of(
        PER, ORG, ORG, PER, PER, LOC, LOC, PER);
    final ImmutableList<Symbol> goldAssignments = ImmutableList.of(
        ORG, LOC, ORG, ORG, PER, LOC, PER, PER);

    final SummaryConfusionMatrices.Builder confusionMatrixB = SummaryConfusionMatrices.builder();

    for (final ZipPair<Symbol, Symbol> systemGoldPair : zip(systemAssignments, goldAssignments)) {
      final Symbol systemSymbol = systemGoldPair.first();
      final Symbol goldSymbol = systemGoldPair.second();

      confusionMatrixB.accumulatePredictedGold(systemSymbol, goldSymbol, 1.0);
    }

    final SummaryConfusionMatrix confusionMatrix = confusionMatrixB.build();

    // If the system just produced the most-common class in the gold (on the RIGHT),
    // we would get an accuracy of 3 / 8
    assertEquals(3.0D / 8.0D, SummaryConfusionMatrices.chooseMostCommonRightHandClassAccuracy(
        confusionMatrix));

    // The reverse of the above doesn't make sense in this system/gold scenario, but assuming
    //  it did, we'd expect 4 / 8 for a majority classification
    assertEquals(4.0D / 8.0D, SummaryConfusionMatrices.chooseMostCommonLeftHandClassAccuracy(
        confusionMatrix));

    // F-measure for PER: 2 true positives, 2 false positives, 1 false negative
    final float perPrec = 2.0F / 4.0F;
    final float perRecall = 2.0F / 3.0F;
    final float perF = (2 * perPrec * perRecall) / (perPrec + perRecall);
    assertEquals(perF,
        SummaryConfusionMatrices.FMeasureVsAllOthers(confusionMatrix, PER).F1());

    // F-measure for ORG: 1 true positive, 1 false positive, 2 false negatives
    final float orgPrec = 1.0F / 2.0F;
    final float orgRecall = 1.0F / 3.0F;
    final float orgF = (2 * orgPrec * orgRecall) / (orgPrec + orgRecall);
    assertEquals(orgF,
        SummaryConfusionMatrices.FMeasureVsAllOthers(confusionMatrix, ORG).F1());

  }
}
