package com.bbn.bue.common.collections;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Set;

/**
 * A Naive implementation of an OverlappingRangeSet - uses O(nlog(n)) operation to construct the
 * object, then uses O(n) operations to find an offending ranges)
 *
 * @author Jay DeYoung
 */
public final class ImmutableOverlappingRangeSet<T extends Comparable<T>>
    implements OverlappingRangeSet<T> {

  private final ImmutableList<Range<T>> ranges;

  private ImmutableOverlappingRangeSet(final Iterable<Range<T>> ranges) {
    this.ranges = ImmutableOverlappingRangeSet.<T>rangeOrdering().immutableSortedCopy(ranges);
  }

  public static <T extends Comparable<T>> ImmutableOverlappingRangeSet<T> create(
      final Iterable<Range<T>> ranges) {
    return new ImmutableOverlappingRangeSet<T>(ranges);
  }

  /**
   * Returns all ranges for which {@link Range#contains(Comparable)} item is true, without
   * preserving multiplicity.
   */
  @Override
  public Collection<Range<T>> rangesContaining(final T item) {
    final ImmutableSet.Builder<Range<T>> ret = ImmutableSet.builder();
    for (final Range<T> range : ranges) {
      if (range.contains(item)) {
        ret.add(range);
      }
    }
    return ret.build();
  }

  /**
   * Finds every range in this object for which {@code range}.{@link Range#encloses(Range)} {@code
   * queryRange}, without preserving multiplicity.
   */
  @Override
  public Collection<Range<T>> rangesContaining(final Range<T> queryRange) {
    final ImmutableSet.Builder<Range<T>> ret = ImmutableSet.builder();
    for (final Range<T> range : ranges) {
      if (range.encloses(queryRange)) {
        ret.add(range);
      }
    }
    return ret.build();
  }

  /**
   * Finds every range in this object for which {@code queryRange}.{@link Range#encloses(Range)},
   * without preserving multiplicity.
   */
  @Override
  public Collection<Range<T>> rangesContainedBy(final Range<T> queryRange) {
    final ImmutableSet.Builder<Range<T>> ret = ImmutableSet.builder();
    for (final Range<T> range : ranges) {
      if (queryRange.encloses(range)) {
        ret.add(range);
      }
    }
    return ret.build();
  }

  /**
   * Returns all {@link Range}s {@link Range#isConnected(Range)} to the {@code queryRange} for which
   * {@link Range#isEmpty()} is false. Does not preserve multiplicity of {@link Range}s
   */
  @Override
  public Collection<Range<T>> rangesOverlapping(final Range<T> queryRange) {
    final ImmutableSet.Builder<Range<T>> ret = ImmutableSet.builder();
    for (final Range<T> range : ranges) {
      if (range.isConnected(queryRange) && !range.intersection(queryRange).isEmpty()) {
        ret.add(range);
      }
    }
    return ret.build();
  }

  private static <T extends Comparable<T>> Ordering<Range<T>> rangeOrdering() {
    return Ordering.natural().onResultOf(RangeUtils.<T>lowerEndPointFunction())
        .compound(Ordering.natural().onResultOf(RangeUtils.<T>upperEndPointFunction()))
        .compound(Ordering.usingToString());
  }

  public static <T extends Comparable<T>> Builder<T> builder() {
    return new Builder<>();
  }

  public static class Builder<T extends Comparable<T>> {

    private final Set<Range<T>> ranges = Sets.newHashSet();

    public void addRange(final Range<T> range) {
      ranges.add(range);
    }

    public void addRanges(final Collection<Range<T>> ranges) {
      this.ranges.addAll(ranges);
    }
  }
}
