package com.bbn.nlp.names;

import com.bbn.bue.common.evaluation.BootstrapInspector;
import com.bbn.bue.common.evaluation.BrokenDownPRFAggregator;
import com.bbn.bue.common.evaluation.EvalPair;
import com.bbn.bue.common.evaluation.ScoringTypedOffsetRange;
import com.bbn.bue.common.evaluation.SummaryConfusionMatrices;
import com.bbn.bue.common.evaluation.SummaryConfusionMatrix;
import com.bbn.bue.common.strings.offsets.Offset;
import com.bbn.bue.common.strings.offsets.OffsetRange;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.bbn.bue.common.evaluation.EvaluationConstants.ABSENT;
import static com.bbn.bue.common.evaluation.EvaluationConstants.PRESENT;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.compose;
import static com.google.common.base.Predicates.equalTo;

/**
 * A bootstrapped scorer for typed extents which gives partial credit according to the degree of
 * overlap.
 *
 * The scoring works as follows: <ul> <li>align each gold name to the system name of the same type
 * sharing the most characters and score a fractional true positive equal to the fraction of
 * characters covered.</li> <li> each system name will score as 1.0 false positives unless there is
 * an overlapping gold name of matching type. If there is, take the gold name which covers the most
 * characters in the system name and score a fractional false positive equal to the fraction of
 * system name characters still uncovered. </li> </ul>
 *
 * Simple examples (use token-based scoring rather than the true character-based scoring for
 * readability): <ul> <li>Gold: "He graduated from the famous [Catholic University of Leuven]"</li>
 * <li>System 1: "He graduated from the [famous Catholic University] of Leuven"</li> <li>System 2:
 * "He graduated from the famous Catholic [University of Leuven]</li> <li>System 3: "He graduated
 * from the famous [Catholic University of Leuven]"</li> </ul> System 1 scores: 0.5 true positives,
 * 0.5 false negatives, 0.33 false positives System 2 scores: 0.75 true positives, 0.25 false
 * negatives, 0.0 false positives System 3: 1.0 true positives, 0.0 false negatives, 0.0 false
 * positives
 */
final class NaivePartialCreditBootstrapStrategy<OffsetType extends Offset<OffsetType>>
    implements
    BootstrapInspector.BootstrapStrategy<EvalPair<Set<ScoringTypedOffsetRange<OffsetType>>,
        Set<ScoringTypedOffsetRange<OffsetType>>>, Map<String, SummaryConfusionMatrix>> {

  private static final Logger log =
      LoggerFactory.getLogger(NaivePartialCreditBootstrapStrategy.class);

  private final File outputDir;
  private final String name;
  private final Function<? super ScoringTypedOffsetRange<OffsetType>, String> breakdownScheme;

  private NaivePartialCreditBootstrapStrategy(String name,
      Function<? super ScoringTypedOffsetRange<OffsetType>, String> breakdownFunction,
      final File outputDir) {
    this.name = checkNotNull(name);
    this.outputDir = checkNotNull(outputDir);
    this.breakdownScheme = checkNotNull(breakdownFunction);
  }

  public static <OffsetType extends Offset<OffsetType>> NaivePartialCreditBootstrapStrategy<OffsetType> createBrokenDownBy(
      final String name,
      final Function<? super ScoringTypedOffsetRange<OffsetType>, String> breakdownFunction,
      File outputDir) {
    return new NaivePartialCreditBootstrapStrategy<>(name, breakdownFunction, outputDir);
  }

  public static <OffsetType extends Offset<OffsetType>> NaivePartialCreditBootstrapStrategy<OffsetType> create(
      final String name, File outputDir) {
    return new NaivePartialCreditBootstrapStrategy<>(name,
        Functions.constant(PRESENT.asString()), outputDir);
  }

  @Override
  public BootstrapInspector.ObservationSummarizer<EvalPair<Set<ScoringTypedOffsetRange<OffsetType>>,
      Set<ScoringTypedOffsetRange<OffsetType>>>, Map<String, SummaryConfusionMatrix>> createObservationSummarizer() {
    return new NaivePartialCreditObservationSummarizer();
  }


  private SummaryConfusionMatrix partialCreditConfusionMatrixFor(
      EvalPair<Set<ScoringTypedOffsetRange<OffsetType>>,
          Set<ScoringTypedOffsetRange<OffsetType>>> evalPair) {
    final SummaryConfusionMatrices.Builder summaryConfusionMatrixB =
        SummaryConfusionMatrices.builder();

    // first, for each item in the key we need to find its best match in the system responses
    for (final ScoringTypedOffsetRange<OffsetType> keyTypedExtent : evalPair.key()) {
      final double bestOverlapFraction =
          computeBestOverlapFraction(keyTypedExtent, evalPair.test());
      // we score a true positive according to how much of the key response is covered
      // and a false negative according to how much is uncovered
      summaryConfusionMatrixB
          .accumulatePredictedGold(PRESENT, PRESENT, bestOverlapFraction);
      summaryConfusionMatrixB
          .accumulatePredictedGold(PRESENT, ABSENT, 1.0 - bestOverlapFraction);
    }

    // now we need to account for false positives by doing the same thing in the other direction
    // for each system response, find its most overlapping key item, and incur a false positive
    // for any uncovered portion
    for (final ScoringTypedOffsetRange<OffsetType> testTypedExtent : evalPair.test()) {
      final double bestOverlapFraction =
          computeBestOverlapFraction(testTypedExtent, evalPair.key());
      summaryConfusionMatrixB.accumulate(ABSENT, PRESENT, 1.0 - bestOverlapFraction);
    }

    return summaryConfusionMatrixB.build();
  }

  private double computeBestOverlapFraction(
      ScoringTypedOffsetRange<OffsetType> reference, Iterable<ScoringTypedOffsetRange<OffsetType>>
      toSearch) {
    // this is an inefficient implementation, but good enough for the scale of data
    // we should receive

    final Predicate<ScoringTypedOffsetRange<OffsetType>> matchesInType = compose(
        equalTo(reference.scoringType()), ScoringTypedOffsetRange.<OffsetType>typeFunction());

    final ImmutableList<ScoringTypedOffsetRange<OffsetType>> searchResponsesOfRightType =
        FluentIterable.from(toSearch).filter(matchesInType).toList();
    if (!searchResponsesOfRightType.isEmpty()) {
      final ScoringTypedOffsetRange<OffsetType> mostOverlapping =
          ScoringTypedOffsetRange.orderByOverlapWith(reference).max(searchResponsesOfRightType);
      // how long is the intersection between the key response and the most overlapping system response?
      final int overlap =
          mostOverlapping.offsetRange().intersection(reference.offsetRange()).transform(
              OffsetRange.lengthFunction()).or(0);
      // we score a true positive according to how much of the key response is covered
      // and a false negative according to how much is uncovered
      final double overlapFraction = overlap / ((double) reference.offsetRange().length());
      return overlapFraction;
    } else {
      // there is no response of matching type, so no overlap
      return 0.0;
    }
  }

  @Override
  public Collection<BootstrapInspector.SummaryAggregator<Map<String, SummaryConfusionMatrix>>> createSummaryAggregators() {
    return ImmutableList.of(prfAggregator());
  }

  public BootstrapInspector.SummaryAggregator<Map<String, SummaryConfusionMatrix>> prfAggregator() {
    return BrokenDownPRFAggregator.create(name, outputDir);
  }

  /**
   * Given a document, this produces confusion matrices for its partial credit name scores.
   */
  private class NaivePartialCreditObservationSummarizer implements
      BootstrapInspector.ObservationSummarizer<EvalPair<Set<ScoringTypedOffsetRange<OffsetType>>,
          Set<ScoringTypedOffsetRange<OffsetType>>>, Map<String, SummaryConfusionMatrix>> {

    @Override
    public Map<String, SummaryConfusionMatrix> summarizeObservation(
        EvalPair<Set<ScoringTypedOffsetRange<OffsetType>>,
            Set<ScoringTypedOffsetRange<OffsetType>>> evalPair) {
      // this just computes partial scores separately for each requested breakdown (e.g.
      // by name type). The real action is in partialCreditConfusionMatrixFor
      final ImmutableMap<String, EvalPair<Set<ScoringTypedOffsetRange<OffsetType>>,
          Set<ScoringTypedOffsetRange<OffsetType>>>> evalPairsByKeys =
          splitByKeys(evalPair, breakdownScheme);
      final ImmutableMap.Builder<String, SummaryConfusionMatrix> ret = ImmutableMap.builder();
      for (final Map.Entry<String, EvalPair<Set<ScoringTypedOffsetRange<OffsetType>>,
          Set<ScoringTypedOffsetRange<OffsetType>>>> e : evalPairsByKeys.entrySet()) {
        ret.put(e.getKey(), partialCreditConfusionMatrixFor(e.getValue()));
      }
      return ret.build();
    }

    private ImmutableMap<String, EvalPair<Set<ScoringTypedOffsetRange<OffsetType>>, Set<ScoringTypedOffsetRange<OffsetType>>>> splitByKeys(
        final EvalPair<Set<ScoringTypedOffsetRange<OffsetType>>, Set<ScoringTypedOffsetRange<OffsetType>>> evalPair,
        final Function<? super ScoringTypedOffsetRange<OffsetType>, String> breakdownScheme) {
      final ImmutableSet<String> allBreakdownKeysSeen = FluentIterable.from(evalPair.key())
          .append(evalPair.test())
          .transform(breakdownScheme)
          .toSet();
      final ImmutableMap.Builder<String, EvalPair<Set<ScoringTypedOffsetRange<OffsetType>>, Set<ScoringTypedOffsetRange<OffsetType>>>>
          ret = ImmutableMap.builder();

      for (final String breakdownKey : allBreakdownKeysSeen) {
        final Set<ScoringTypedOffsetRange<OffsetType>> filteredKey =
            FluentIterable.from(evalPair.key())
                .filter(compose(equalTo(breakdownKey), breakdownScheme))
                .toSet();
        final Set<ScoringTypedOffsetRange<OffsetType>> filteredTest =
            FluentIterable.from(evalPair.test())
                .filter(compose(equalTo(breakdownKey), breakdownScheme))
                .toSet();
        ret.put(breakdownKey, EvalPair.of(filteredKey, filteredTest));
      }

      return ret.build();
    }
  }

}
