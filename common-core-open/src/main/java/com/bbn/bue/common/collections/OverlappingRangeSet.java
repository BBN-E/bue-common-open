package com.bbn.bue.common.collections;

import com.google.common.collect.Range;

import java.util.Collection;

/**
 * A class that supports overlapping {@code Range<T>}
 * All operations support the closedness notion in {@link Range}.
 */
public interface OverlappingRangeSet<T extends Comparable<T>> {

  Collection<Range<T>> rangesContaining(T item);
  Collection<Range<T>> rangesContaining(Range<T> queryRange);
  Collection<Range<T>> rangesContained(Range<T> queryRange);
  Collection<Range<T>> rangesOverlapped(Range<T> queryRange);
}
