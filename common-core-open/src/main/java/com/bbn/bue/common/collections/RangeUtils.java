package com.bbn.bue.common.collections;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

/**
 * Created by jdeyoung on 6/19/15.
 */
public class RangeUtils {

  public static boolean isClosed(final Range<?> source) {
    return source.upperBoundType().equals(BoundType.CLOSED) && source.lowerBoundType()
        .equals(BoundType.CLOSED);
  }
}
