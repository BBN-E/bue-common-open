package com.bbn.bue.common.collections;

import com.google.common.collect.Range;

import java.util.Collection;

/**
 * A container for a set of potentially overlapping {@code Range<T>} which supports various
 * containment queries. All operations support the closedness notion of the inserted {@link
 * Range}s.
 *
 * Guava supports collections of disjoint and nested ranges, see {@link
 * com.google.common.collect.RangeSet} and {@link com.google.common.collect.TreeRangeSet}, however
 * there is no current implementation for a structured set of {@link Range}s which may overlap. This
 * class aims to fill that gap.
 *
 * @author Jay DeYoung
 */
public interface OverlappingRangeSet<T extends Comparable<T>> {

  /**
   * Returns a {@link Collection} of ranges containing this item. Multiplicity preservation is
   * implementation dependent.
   */
  Collection<Range<T>> rangesContaining(T item);

  /**
   * Returns a {@link Collection} of ranges containing this query range. Multiplicity preservation
   * is implementation dependent.
   */
  Collection<Range<T>> rangesContaining(Range<T> queryRange);

  /**
   * Returns a {@link Collection} of ranges contained by this query range. Multiplicity preservation
   * is implementation dependent.
   */
  Collection<Range<T>> rangesContainedBy(Range<T> queryRange);

  /**
   * Returns a {@link Collection} of ranges overlapping this query range. Multiplicity preservation
   * is implementation dependent.
   */
  Collection<Range<T>> rangesOverlapping(Range<T> queryRange);
}
