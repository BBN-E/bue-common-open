package com.bbn.bue.common.collections;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

/**
 * Utility methods for dealing with Guava {@link Range}s.
 */
public final class RangeUtils {
  private RangeUtils() {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns true iff {@code range} is bounded and closed on both sides.  {@code range}
   * may not be null.
   */
  public static boolean isClosed(final Range<?> range) {
    return range.hasUpperBound() && BoundType.CLOSED.equals(range.upperBoundType())
        && range.hasLowerBound() && BoundType.CLOSED.equals(range.lowerBoundType());
  }
}
