package com.bbn.bue.common.evaluation;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.Map;

import static com.bbn.bue.common.evaluation.EvaluationConstants.ABSENT;
import static com.bbn.bue.common.evaluation.EvaluationConstants.PRESENT;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A bootstrap sampling strategy for any metric computable from a binary confusion matrix.
 * This also supports breaking down the output based on the results of some provided function.
 *
 * An item is consider a true positive if it has an alignment in the key.  Each unaligned test item
 * is considered to be a false positive and each unaligned key item is considered to be a false
 * negative.  The aggregators are responsible from processing these counts into some score which is
 * bootstrapped.
 *
 * If a breakdown function is specified, separate results will be produced for each output of that
 * function where the alignment is filtered to include only items with the same value from the
 * function (e.g. to break down results by event type).
 *
 */
public final class BinaryConfusionMatrixBootstrapStrategy<T> implements
    BootstrapInspector.BootstrapStrategy<Alignment<? extends T, ? extends T>, Map<String, FMeasureCounts>> {

  private final Function<? super T, String> breakdownScheme;
  private final ImmutableSet<BootstrapInspector.SummaryAggregator<Map<String, FMeasureCounts>>> summaryAggregators;

  private BinaryConfusionMatrixBootstrapStrategy(
      final Function<? super T, String> breakdownScheme,
      final ImmutableSet<? extends BootstrapInspector.SummaryAggregator<Map<String, FMeasureCounts>>> summaryAggregators) {
    this.breakdownScheme = checkNotNull(breakdownScheme);
    this.summaryAggregators = ImmutableSet.copyOf(summaryAggregators);
  }

  public static <T> BinaryConfusionMatrixBootstrapStrategy<T> create(
      final Function<? super T, String> breakdownScheme,
      final ImmutableSet<? extends BootstrapInspector.SummaryAggregator<Map<String, FMeasureCounts>>> summaryAggregators) {
    return new BinaryConfusionMatrixBootstrapStrategy<>(breakdownScheme, summaryAggregators);
  }

  @Override
  public BootstrapInspector.ObservationSummarizer<Alignment<? extends T, ? extends T>, Map<String, FMeasureCounts>> createObservationSummarizer() {
    return new BootstrapInspector.ObservationSummarizer<Alignment<? extends T, ? extends T>, Map<String, FMeasureCounts>>() {
      @Override
      public Map<String, FMeasureCounts> summarizeObservation(
          final Alignment<? extends T, ? extends T> alignment) {
        final ImmutableMap<String, Alignment<T, T>> alignmentsByKeys =
            Alignments.splitAlignmentByKeyFunction(alignment, breakdownScheme);
        final ImmutableMap.Builder<String, FMeasureCounts> ret = ImmutableMap.builder();
        for (final Map.Entry<String, Alignment<T, T>> e : alignmentsByKeys.entrySet()) {
          ret.put(e.getKey(), confusionMatrixForAlignment(e.getValue()));
        }
        return ret.build();
      }
    };
  }

  private FMeasureCounts confusionMatrixForAlignment(
      final Alignment<? extends T, ? extends T> alignment) {
    final SummaryConfusionMatrices.Builder summaryConfusionMatrixB =
        SummaryConfusionMatrices.builder();
    summaryConfusionMatrixB
        .accumulatePredictedGold(PRESENT, PRESENT, alignment.rightAligned().size());
    summaryConfusionMatrixB
        .accumulatePredictedGold(ABSENT, PRESENT, alignment.leftUnaligned().size());
    summaryConfusionMatrixB
        .accumulatePredictedGold(PRESENT, ABSENT, alignment.rightUnaligned().size());
    return SummaryConfusionMatrices.FMeasureVsAllOthers(summaryConfusionMatrixB.build(), PRESENT);
  }

  @Override
  public Collection<BootstrapInspector.SummaryAggregator<Map<String, FMeasureCounts>>> createSummaryAggregators() {
    return summaryAggregators;
  }
}
