package com.bbn.bue.common;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;

import org.immutables.value.Value;

import java.util.List;

import javax.annotation.Nullable;

/**
 * For documentation, see {@link OrderingUtils#explicitOrderingUnrankedLast(List)}
 * and friends.
 */
@TextGroupImmutable
@Value.Immutable
abstract class ExplicitOrderingNonExclusive<T> extends Ordering<T> {

  abstract ImmutableMap<T, Integer> rankMap();

  @Value.Default
  boolean unrankedIsFirst() {
    return true;
  }

  @Override
  public final int compare(@Nullable final T left, @Nullable final T right) {
    final Integer leftRank = rankMap().get(left);
    final Integer rightRank = rankMap().get(right);

    if (leftRank != null && rightRank != null) {
      return leftRank - rightRank; // safe because both non-negative
    } else if (leftRank != null) {
      if (unrankedIsFirst()) {
        return 1;
      } else {
        return -1;
      }
    } else if (rightRank != null) {
      if (unrankedIsFirst()) {
        return -1;
      } else {
        return 1;
      }
    } else {
      // if an ordering is not specified between two items, they are considered equal
      return 0;
    }
  }

  @Override
  public final String toString() {
    final String unrankedString = unrankedIsFirst() ? "first" : "last";
    return "explicitOrdering(" + rankMap().keySet() + ", unranked=" + unrankedString + ")";
  }
}
