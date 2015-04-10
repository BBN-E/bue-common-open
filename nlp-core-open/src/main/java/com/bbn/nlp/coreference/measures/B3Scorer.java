package com.bbn.nlp.coreference.measures;

import com.bbn.bue.common.collections.CollectionUtils;
import com.bbn.bue.common.evaluation.FMeasureInfo;
import com.bbn.bue.common.evaluation.PrecisionRecallPair;

import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implements the B^3 coreference algorithm (Bagga and Baldwin, 1998)
 *
 * Elements being clustered are assumed to have properly defined equality and hashCode methods.
 *
 * This is unit-tested along with the other coref metrics in BBN-internal code.
 */
public final class B3Scorer {

  private enum B3Method {ByCluster, ByElement}

  private B3Scorer(final B3Method method) {
    this.method = checkNotNull(method);
  }

  public static B3Scorer createByElementScorer() {
    return new B3Scorer(B3Method.ByElement);
  }

  public FMeasureInfo score(final Iterable<? extends Iterable<?>> predicted,
      final Iterable<? extends Iterable<?>> gold) {
    final Iterable<Set<Object>> predictedAsSets = CorefScorerUtils.toSets(predicted);
    final Iterable<Set<Object>> goldAsSets = CorefScorerUtils.toSets(gold);

    if (method == B3Method.ByElement) {
      return scoreSets(predictedAsSets, goldAsSets);
    } else {
      throw new RuntimeException("B3Method.ByCluster not yet implemented");
    }
  }

  private FMeasureInfo scoreSets(final Iterable<Set<Object>> predicted,
      final Iterable<Set<Object>> gold) {
    final Map<Object, Set<Object>> predictedItemToGroup =
        CollectionUtils.makeElementsToContainersMap(predicted);
    final Map<Object, Set<Object>> goldItemToGroup =
        CollectionUtils.makeElementsToContainersMap(gold);

    CorefScorerUtils.checkPartitionsOverSameElements(predictedItemToGroup.keySet(),
        goldItemToGroup.keySet());

    // if this is empty, we know the other is too,
    // by the above
    if (predictedItemToGroup.isEmpty()) {
      return new PrecisionRecallPair(0.0f, 0.0f);
    }

    double precisionTotal = 0.0;
    double recallTotal = 0.0;
    for (final Object item : goldItemToGroup.keySet()) {
      final Set<Object> goldGroup = goldItemToGroup.get(item);
      final Set<Object> predictedGroup = predictedItemToGroup.get(item);
      final Set<Object> inBoth = Sets.intersection(goldGroup, predictedGroup);

      precisionTotal += inBoth.size() / ((double) predictedGroup.size());
      recallTotal += inBoth.size() / ((double) goldGroup.size());
    }

    return new PrecisionRecallPair(
        (float) (precisionTotal / goldItemToGroup.keySet().size()),
        (float) (recallTotal / goldItemToGroup.keySet().size()));
  }

  private final B3Method method;
}

