package com.bbn.bue.common.collections;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

/**
 * Created by jdeyoung on 6/19/15.
 */
public class RangeUtils {

  public static boolean isClosed(final Range<?> range) {
    return range.hasUpperBound() && BoundType.CLOSED.equals(range.upperBoundType())
        && range.hasLowerBound() && BoundType.CLOSED.equals(range.lowerBoundType());
  }
}
