package com.bbn.nlp.coreference.measures;

import com.bbn.bue.common.collections.CollectionUtils;
import com.bbn.bue.common.evaluation.FMeasureInfo;
import com.bbn.bue.common.evaluation.PrecisionRecallPair;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Produces coreference scores according to the MUC metric.
 *
 * See Marc Vilain, John Burger, John Aberdeen, Dennis Connolly, and Lynette Hirschman. 1995. A
 * model-theoretic coreference scoring scheme. In Proceedings fo the 6th Message Understanding
 * Conference (MUC6).
 */
public final class MUCScorer {

  private MUCScorer() {
  }

  public static MUCScorer create() {
    return new MUCScorer();
  }

  /**
   * Returns absent if the score is undefined (e.g. one side is all singletons).
   */
  public Optional<FMeasureInfo> score(final Iterable<? extends Iterable<?>> predicted,
      final Iterable<? extends Iterable<?>> gold) {
    final List<Set<Object>> predictedAsSets = CorefScorerUtils.toSets(predicted);
    final List<Set<Object>> goldAsSets = CorefScorerUtils.toSets(gold);

    final Map<Object, Set<Object>> predictedItemToGroup =
        CollectionUtils.makeElementsToContainersMap(predictedAsSets);
    final Map<Object, Set<Object>> goldItemToGroup =
        CollectionUtils.makeElementsToContainersMap(goldAsSets);

    CorefScorerUtils.checkPartitionsOverSameElements(predictedItemToGroup.keySet(),
        goldItemToGroup.keySet());

    final Optional<Float> recall =
        mucScoreComponent(goldAsSets, predictedAsSets, predictedItemToGroup);
    final Optional<Float> precision =
        mucScoreComponent(predictedAsSets, goldAsSets, goldItemToGroup);

    if (recall.isPresent() && precision.isPresent()) {
      // why are these floats?
      return Optional.<FMeasureInfo>of(new PrecisionRecallPair(precision.get(), recall.get()));
    } else {
      return Optional.absent();
    }
  }

  private Optional<Float> mucScoreComponent(List<Set<Object>> leftClustering,
      List<Set<Object>> rightClustering,
      Map<Object, Set<Object>> rightItemToGroup) {
    int numerator = 0;
    int denominator = 0;

    for (final Set<Object> leftCluster : leftClustering) {
      final Set<Set<Object>> rightClustersThisOneOverlaps = Sets.newHashSet();
      for (final Object leftClusterItem : leftCluster) {
        rightClustersThisOneOverlaps.add(rightItemToGroup.get(leftClusterItem));
      }

      final int overlapSize = rightClustersThisOneOverlaps.size();
      final int leftClusterSize = leftCluster.size();

      numerator += leftClusterSize - overlapSize;
      denominator += leftClusterSize - 1;
    }

    if (denominator > 0) {
      return Optional.of(((float) numerator) / denominator);
    } else {
      return Optional.absent();
    }
  }
}
