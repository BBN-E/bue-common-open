package com.bbn.nlp.corpora.OffsetMapping;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.bbn.bue.common.collections.RangeUtils.isClosed;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Default implementations of certain methods. They may not match precisely the semantics of your
 * mapper.
 */
public abstract class AbstractOffsetMapper implements OffsetMapper {

  /**
   * @param closedSource - a range that is closed constructed as offset, offset + length - 1
   * @return the cartesian product of {start mappings} and {end mappings}
   */
  @Override
  public Collection<Range<Integer>> mapRange(final Range<Integer> closedSource) {
    checkArgument(isClosed(closedSource));
    final ImmutableSet.Builder<Range<Integer>> resultRanges = ImmutableSet.builder();
    if (isEntirelyMapped(closedSource)) {
      Set<Integer> start = ImmutableSet.copyOf(mapOffset(closedSource.lowerEndpoint()));
      Set<Integer> end = ImmutableSet.copyOf(mapOffset(closedSource.upperEndpoint()));
      for (List<Integer> pair : Sets.cartesianProduct(start, end)) {
        resultRanges.add(Range.closed(pair.get(0), pair.get(1)));
      }
    }
    return resultRanges.build();
  }

  /**
   * throws an Exception via checkState if there's more than one mapping
   *
   * @return the single mapping for a range, Optional.absent() if there are none
   */
  public Optional<Range<Integer>> mapRangeExactlyOnce(final Range<Integer> closedSource) {
    checkArgument(isClosed(closedSource));
    final List<Range<Integer>> results = ImmutableList.copyOf(mapRange(closedSource));
    checkState(results.size() <= 1);
    if (results.size() == 0) {
      return Optional.absent();
    }
    return Optional.of(results.get(0));
  }

  /**
   * throws an Exception via checkState if there's more than one mapping
   *
   * @return the single mapping for this, if any
   */
  public Optional<Integer> mapOffsetExactlyOnce(final int source) {
    if (isMapped(source)) {
      final List<Integer> mappings = ImmutableList.copyOf(mapOffset(source));
      checkState(mappings.size() <= 1);
      if (mappings.size() == 0) {
        return Optional.absent();
      }
      return Optional.of(mappings.get(0));
    }
    return Optional.absent();
  }

  /**
   * returns true unless an index in this range is unmapped
   */
  @Override
  public boolean isEntirelyMapped(final Range<Integer> source) {
    checkArgument(isClosed(source));
    for (int i = source.lowerEndpoint(); i < source.upperEndpoint(); i++) {
      if (!isMapped(i)) {
        return false;
      }
    }
    return true;
  }
}
