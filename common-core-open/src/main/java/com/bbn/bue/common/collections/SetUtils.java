package com.bbn.bue.common.collections;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * Utilities related to {@link java.util.Set}s.
 */
public final class SetUtils {

  private SetUtils() {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns all items found in the {@code left} iterable not found in the {@code right}.  If the
   * iterables are not deterministic, this will reflect a single pass through each.  This is not a
   * view.
   */
  public static <T> Sets.SetView<T> differenceAsSets(Iterable<T> left, Iterable<T> right) {
    return Sets.difference(ImmutableSet.copyOf(left), ImmutableSet.copyOf(right));
  }
}
