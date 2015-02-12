package com.bbn.nlp.coreference.measures;

import com.bbn.bue.common.collections.CollectionUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

/* package-private */ final class StandardBLANCScorer implements BLANCScorer {

  public BLANCResult score(final Iterable<? extends Iterable<?>> predicted,
      final Iterable<? extends Iterable<?>> gold) {
    final Iterable<Set<Object>> predictedAsSets = CorefScorerUtils.toSets(predicted);
    final Iterable<Set<Object>> goldAsSets = CorefScorerUtils.toSets(gold);

    return scoreSets(predictedAsSets, goldAsSets);
  }

  private BLANCResult scoreSets(final Iterable<Set<Object>> predicted,
      final Iterable<Set<Object>> gold) {

    final Map<Object, Set<Object>> predictedItemToGroup =
        CollectionUtils.makeElementsToContainersMap(predicted);
    final Map<Object, Set<Object>> goldItemToGroup =
        CollectionUtils.makeElementsToContainersMap(gold);

    CorefScorerUtils.checkPartitionsOverSameElements(predictedItemToGroup.keySet(),
        goldItemToGroup.keySet());

    // number of pairwise coreference links agreed on by system and gold
    int rc = 0;
    // number of pairwise non-coreference links agreed on by system and gold
    int rn = 0;
    // number of pairwise links claimed by gold but not system
    int wn = 0;
    // number of pairwise links claimed by system but not gold
    int wc = 0;

    double corefLinksInBoth = 0.0;
    double corefLinksInKey = 0.0;
    double corefLinksInResponse = 0.0;
    double nonCorefInBoth = 0.0;
    double nonCorefLinksInKey = 0.0;
    double nonCorefLinksInResponse = 0.0;

    final Set<Object> allItems = predictedItemToGroup.keySet();
    for (final Object item : allItems) {
      final Set<Object> predictedNeighbors = Sets.newHashSet(predictedItemToGroup.get(item));
      predictedNeighbors.remove(item);
      final Set<Object> goldNeighbors = Sets.newHashSet(goldItemToGroup.get(item));
      goldNeighbors.remove(item);


      // The contribution for this item is the size of the intersection
      // of the gold and predicted neighbor sets.
      corefLinksInBoth += Sets.intersection(predictedNeighbors, goldNeighbors).size();
      corefLinksInResponse += predictedNeighbors.size();
      corefLinksInKey += goldNeighbors.size();
        // -1 = don't count this item itself as a link
        nonCorefLinksInKey += allItems.size() - goldNeighbors.size() - 1;

        // -1 = don't count this item itself as a link
        nonCorefLinksInResponse += allItems.size() - predictedNeighbors.size() - 1;

        final ImmutableSet<Object> neighborsInEither =
            Sets.union(predictedNeighbors, goldNeighbors).immutableCopy();
        // -1 = don't count this item itself as a link
        nonCorefInBoth += Sets.difference(allItems, neighborsInEither).size() - 1;
      /*final Set<Object> predictedCluster = predictedItemToGroup.get(item);
      final Set<Object> goldCluster = goldItemToGroup.get(item);

      // the number of correct coref links for this item is the size of the intersection
      // of the gold and predicted clusters, minus this item itself.
      final int goldPredictedIntersectionSize =
          Sets.intersection(predictedCluster, goldCluster).size();
      rc += goldPredictedIntersectionSize - 1;
      // the number of links claimed by the system but not gold (wc)
      // is the size of the difference between the system and gold cluster
      wc += Sets.difference(predictedCluster, goldCluster).size();
      // the number of links claimed by gold but not the system (wn)
      // is the size of the difference between the gold and system cluster
      wn += Sets.difference(goldCluster, predictedCluster).size();
      // the number of coref non-coreference links is the number of items minus the items in the union of
      // the gold and predicted clusters
      final int goldPredictedUnionSize =
          predictedCluster.size() + goldCluster.size() - goldPredictedIntersectionSize;
      rn += allItems.size() - goldPredictedUnionSize;*/

    }

    return BLANCResult.fromSetCounts(true,
        corefLinksInBoth, corefLinksInKey, corefLinksInResponse, nonCorefInBoth,
        nonCorefLinksInKey, nonCorefLinksInResponse);
  }
}
