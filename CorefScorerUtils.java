package com.bbn.nlp.coreference.measures;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

/* package-private*/ final class CorefScorerUtils {
    private CorefScorerUtils() {
    }


    static void checkPartitionsOverSameElements(
            final Set<Object> predictedItems,
            final Set<Object> goldItems) {
        if (!predictedItems.equals(goldItems)) {
            final Set<Object> predictedButNotGold = Sets.difference(
                    predictedItems, goldItems);
            final Set<Object> goldButNotPredicted = Sets.difference(
                    goldItems, predictedItems);
            throw new RuntimeException(String.format(
                "Elements in partitions must match. In predicted but not gold: %s. In gold but not predicted: %s",
                predictedButNotGold, goldButNotPredicted));
        }
    }

    static List<Set<Object>> toSets(final Iterable<? extends Iterable<?>> iterables)
    {
        final ImmutableList.Builder<Set<Object>> ret =
                ImmutableList.builder();

        for (final Iterable<?> iterable : iterables) {
            ret.add(ImmutableSet.copyOf(iterable));
        }

        return ret.build();
    }
}
