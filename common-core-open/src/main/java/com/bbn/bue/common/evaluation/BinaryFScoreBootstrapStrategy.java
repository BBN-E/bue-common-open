package com.bbn.bue.common.evaluation;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import static com.bbn.bue.common.evaluation.EvaluationConstants.ABSENT;
import static com.bbn.bue.common.evaluation.EvaluationConstants.PRESENT;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A bootstrap sampling strategy for precision, recall, F-score, and accuracy.  This also supports
 * breaking down the output based on the results of some provided function.
 *
 * An item is consider a true positive if it has an alignment in the key.  Each unaligned test item
 * is considered to be a false positive and each unaligned key item is considered to be a false
 * negative.
 *
 * If a breakdown function is specified, separate results will be produced for each output of that
 * function where the alignment is filtered to include only items with the same value from the
 * function (e.g. to break down results by event type).
 */
@Beta
public final class BinaryFScoreBootstrapStrategy<T>
    implements
    BootstrapInspector.BootstrapStrategy<Alignment<? extends T, ? extends T>, Map<String, SummaryConfusionMatrix>> {

  private final File outputDir;
  private final String name;
  private final Function<? super T, String> breakdownScheme;

  private BinaryFScoreBootstrapStrategy(String name, Function<? super T, String> breakdownFunction,
      final File outputDir) {
    this.name = checkNotNull(name);
    this.outputDir = checkNotNull(outputDir);
    this.breakdownScheme = checkNotNull(breakdownFunction);
  }

  public static <T> BinaryFScoreBootstrapStrategy<T> createBrokenDownBy(
      final String name, final Function<? super T, String> breakdownFunction, File outputDir) {
    return new BinaryFScoreBootstrapStrategy<T>(name, breakdownFunction, outputDir);
  }

  public static <T> BinaryFScoreBootstrapStrategy<T> create(
      final String name, File outputDir) {
    return new BinaryFScoreBootstrapStrategy<T>(name, Functions.constant("Present"), outputDir);
  }

  @Override
  public BootstrapInspector.ObservationSummarizer<Alignment<? extends T, ? extends T>, Map<String, SummaryConfusionMatrix>> createObservationSummarizer() {
    return new BootstrapInspector.ObservationSummarizer<Alignment<? extends T, ? extends T>, Map<String, SummaryConfusionMatrix>>() {
      @Override
      public Map<String, SummaryConfusionMatrix> summarizeObservation(
          final Alignment<? extends T, ? extends T> alignment) {
        final ImmutableMap<String, Alignment<T, T>> alignmentsByKeys =
            Alignments.splitAlignmentByKeyFunction(alignment, breakdownScheme);
        final ImmutableMap.Builder<String, SummaryConfusionMatrix> ret = ImmutableMap.builder();
        for (final Map.Entry<String, Alignment<T, T>> e : alignmentsByKeys.entrySet()) {
          ret.put(e.getKey(), confusionMatrixForAlignment(e.getValue()));
        }
        return ret.build();
      }
    };
  }

  private SummaryConfusionMatrix confusionMatrixForAlignment(
      final Alignment<? extends T, ? extends T> alignment) {
    final SummaryConfusionMatrices.Builder summaryConfusionMatrixB =
        SummaryConfusionMatrices.builder();
    summaryConfusionMatrixB
        .accumulatePredictedGold(PRESENT, PRESENT, alignment.rightAligned().size());
    summaryConfusionMatrixB
        .accumulatePredictedGold(ABSENT, PRESENT, alignment.leftUnaligned().size());
    summaryConfusionMatrixB
        .accumulatePredictedGold(PRESENT, ABSENT, alignment.rightUnaligned().size());
    return summaryConfusionMatrixB.build();
  }

  @Override
  public Collection<BootstrapInspector.SummaryAggregator<Map<String, SummaryConfusionMatrix>>> createSummaryAggregators() {
    return ImmutableList.of(prfAggregator());
  }

  public BootstrapInspector.SummaryAggregator<Map<String, SummaryConfusionMatrix>> prfAggregator() {
    return BrokenDownPRFAggregator.create(name, outputDir);
  }
}

