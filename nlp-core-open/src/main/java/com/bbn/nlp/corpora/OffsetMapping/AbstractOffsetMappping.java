package com.bbn.nlp.corpora.OffsetMapping;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
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
public abstract class AbstractOffsetMappping implements OffsetMapping {

  @Override
  public Collection<Range<Integer>> mapRange(final Range<Integer> closedSource) {
    checkArgument(isClosed(closedSource));

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

abstract class AbstractFunctionalOffsetMapping extends AbstractOffsetMappping
    implements FunctionalOffsetMapping {

  public Optional<Integer> mapOffsetUniquely(int sourceIdx) {
    final Collection<Integer> mappings = mapOffset(sourceIdx);
    checkState(mappings.size() < 2,
        "%s does not obey the requirements of the FunctionalOffsetMapping"
            + " interface: %s maps to %s", this.getClass(), sourceIdx, mappings);
    if (!mappings.isEmpty()) {
      // will never be null by check above
      return Optional.of(Iterables.getFirst(mappings, null));
    } else {
      return Optional.absent();
    }
  }
}
