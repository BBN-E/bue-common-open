package com.bbn.nlp.coreference.measures;

import com.bbn.bue.common.collections.CollectionUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Set;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.concat;

/**
 * Scores coreference clusterings by a variant of the BLANC measure which allows both different
 * items sets in the key and the reference and for the same item to appear in multiple clusters.
 *
 * The differing items case is handled by Luo et al. "An Extension of BLANC to System Mentions." ACL
 * 2014, whose approach we follow. The extension to items appearing in multiple clusters is our
 * own.
 *
 * See Marta Recasens Potau.  "Coreference: Theory, Annotation, Resolution, and Evaluation." PhD.
 * Dissertation. University of Barcelona http://stel.ub.edu/cba2010/phd/phd.pdf
 */
public final class MultiBLANCScorer implements BLANCScorer {

  public BLANCResult score(final Iterable<? extends Iterable<?>> predicted,
      final Iterable<? extends Iterable<?>> gold) {
    final Iterable<Set<Object>> predictedAsSets = CorefScorerUtils.toSets(predicted);
    final Iterable<Set<Object>> goldAsSets = CorefScorerUtils.toSets(gold);

    return scoreSets(predictedAsSets, goldAsSets);
  }

  private BLANCResult scoreSets(final Iterable<Set<Object>> predicted,
      final Iterable<Set<Object>> gold) {

    final Multimap<Object, Set<Object>> predictedItemToGroup =
        CollectionUtils.makeSetElementsToContainersMultimap(predicted);
    final Multimap<Object, Set<Object>> goldItemToGroup =
        CollectionUtils.makeSetElementsToContainersMultimap(gold);

    final Set<Object> keyItems = goldItemToGroup.keySet();
    final Set<Object> responseItems = predictedItemToGroup.keySet();
    final ImmutableSet<Object> itemsInBoth =
        Sets.intersection(keyItems, responseItems).immutableCopy();

    // |C_k \cap C_r|
    int corefLinksInBoth = 0;
    // |C_k|
    int corefLinksInKey = 0;
    // |C_r|
    int corefLinksInResponse = 0;
    // |N_K \cap N_r|
    int nonCorefInBoth = 0;
    // |N_k|
    int nonCorefLinksInKey = 0;
    // |N_r|
    int nonCorefLinksInResponse = 0;

    final Set<Object> allItems = Sets.union(responseItems,
        keyItems).immutableCopy();

    for (final Object item : allItems) {
      final boolean inKey = keyItems.contains(item);
      final boolean inResponse = responseItems.contains(item);

      final Collection<Set<Object>> predictedClusters = predictedItemToGroup.get(item);
      final Collection<Set<Object>> goldClusters = goldItemToGroup.get(item);

      final Predicate<Object> NOT_ITSELF = not(equalTo(item));
      final ImmutableSet<Object> predictedNeighbors =
          FluentIterable.from(concat(predictedClusters)).filter(NOT_ITSELF).toSet();
      final ImmutableSet<Object> goldNeighbors =
          FluentIterable.from(concat(goldClusters)).filter(NOT_ITSELF).toSet();

      // The contribution for this item is the size of the intersection
      // of the gold and predicted neighbor sets.
      corefLinksInBoth += Sets.intersection(predictedNeighbors, goldNeighbors).size();
      corefLinksInResponse += predictedNeighbors.size();
      corefLinksInKey += goldNeighbors.size();
      if (inKey) {
        // -1 = don't count this item itself as a link
        nonCorefLinksInKey += keyItems.size() - goldNeighbors.size() - 1;
      }

      if (inResponse) {
        // -1 = don't count this item itself as a link
        nonCorefLinksInResponse += responseItems.size() - predictedNeighbors.size() - 1;
      }

      if (inKey && inResponse) {
        final ImmutableSet<Object> neighborsInEither =
            Sets.union(predictedNeighbors, goldNeighbors).immutableCopy();
        // -1 = don't count this item itself as a link
        nonCorefInBoth += Sets.difference(itemsInBoth, neighborsInEither).size() - 1;
      }
    }

    return BLANCResult.fromSetCounts(keyItems.equals(responseItems),
        corefLinksInBoth, corefLinksInKey, corefLinksInResponse, nonCorefInBoth,
        nonCorefLinksInKey, nonCorefLinksInResponse);
  }
}
