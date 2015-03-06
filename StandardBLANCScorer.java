package com.bbn.nlp.coreference.measures;

import com.bbn.bue.common.collections.CollectionUtils;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

@Beta
/* package-private */ final class StandardBLANCScorer implements BLANCScorer {
  private final boolean useSelfEdges;

  /* package-private */ StandardBLANCScorer(boolean useSelfEdges) {
    this.useSelfEdges = useSelfEdges;
  }

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

    double corefLinksInBoth = 0.0;
    double corefLinksInKey = 0.0;
    double corefLinksInResponse = 0.0;
    double nonCorefInBoth = 0.0;
    double nonCorefLinksInKey = 0.0;
    double nonCorefLinksInResponse = 0.0;

    final Set<Object> allItems = predictedItemToGroup.keySet();
    for (final Object item : allItems) {
      final Set<Object> predictedNeighbors = Sets.newHashSet(predictedItemToGroup.get(item));
      if (!useSelfEdges) {
        predictedNeighbors.remove(item);
      }
      final Set<Object> goldNeighbors = Sets.newHashSet(goldItemToGroup.get(item));
      if (!useSelfEdges) {
        goldNeighbors.remove(item);
      }


      // The contribution for this item is the size of the intersection
      // of the gold and predicted neighbor sets.
      corefLinksInBoth += Sets.intersection(predictedNeighbors, goldNeighbors).size();
      corefLinksInResponse += predictedNeighbors.size();
      corefLinksInKey += goldNeighbors.size();
        // -1 = don't count this item itself as a link if not using self edges
        nonCorefLinksInKey += allItems.size() - goldNeighbors.size()  + (useSelfEdges?0:- 1);

        // -1 = don't count this item itself as a link if not using self edges
        nonCorefLinksInResponse += allItems.size() - predictedNeighbors.size() + (useSelfEdges?0:-1);

        final ImmutableSet<Object> neighborsInEither =
            Sets.union(predictedNeighbors, goldNeighbors).immutableCopy();
        // -1 = don't count this item itself as a link if not using self-edgescd 
        nonCorefInBoth += Sets.difference(allItems, neighborsInEither).size() + (useSelfEdges?0:-1);
    }

    return BLANCResult.fromSetCounts(true,
        corefLinksInBoth, corefLinksInKey, corefLinksInResponse, nonCorefInBoth,
        nonCorefLinksInKey, nonCorefLinksInResponse);
  }
}
