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
    final ImmutableList<Symbol> goldAssignments = ImmutableList.of(
        PER, LOC, ORG, ORG, ORG, LOC, PER, PER);
    final ImmutableList<Symbol> systemAssignments = ImmutableList.of(
        PER, ORG, ORG, PER, ORG, LOC, PER, LOC);

    final SummaryConfusionMatrices.Builder confusionMatrixB = SummaryConfusionMatrices.builder();

    for (final ZipPair<Symbol, Symbol> goldSystemPair : zip(goldAssignments, systemAssignments)) {
      final Symbol goldSymbol = goldSystemPair.first();
      final Symbol systemSymbol = goldSystemPair.second();

      confusionMatrixB.accumulatePredictedGold(systemSymbol, goldSymbol, 1.0);
    }

    final SummaryConfusionMatrix confusionMatrix = confusionMatrixB.build();

    // F-measure for PER: 2 true positives, 1 false positive, 1 false negative
    // precision = 2/3, recall = 2/3, F = 2/3
    assertEquals(2.0F / 3.0F,
        SummaryConfusionMatrices.FMeasureVsAllOthers(confusionMatrix, PER).F1());

    // F-measure for ORG: 2 true positives, 1 false positive, 1 false negative
    // precision = 2/3, recall = 2/3, F = 2/3
    assertEquals(2.0F / 3.0F,
        SummaryConfusionMatrices.FMeasureVsAllOthers(confusionMatrix, ORG).F1());

    // F-measure for LOC: 1 true positive, 1 false positive, 1 false negative
    // precision = 1/2, recall = 1/2, F=1/2
    assertEquals(0.5F, SummaryConfusionMatrices.FMeasureVsAllOthers(confusionMatrix, LOC).F1());
  }
}
