package com.bbn.bue.common.collections;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import java.util.Iterator;

/**
 * Utility methods for dealing with Guava {@link Range}s.
 *
 * @author Jay DeYoung, Ryan Gabbard
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

  /**
   * Returns the minimal range that {@link Range#encloseAll(Iterable)} {@code ranges}.
   *
   * You will want to do something smarter if you have many ranges. Will return {@link
   * Optional#absent()} if {@code ranges} is empty.
   *
   * Warning: the current implementation is targeted for small numbers of ranges only. See
   * https://github.com/google/guava/issues/2088
   */
  public static <T extends Comparable<T>> Optional<Range<T>> span(Iterable<Range<T>> ranges) {
    final Iterator<Range<T>> it = ranges.iterator();
    if (it.hasNext()) {
      Range<T> ret = it.next();
      while (it.hasNext()) {
        ret = ret.span(it.next());
      }
      return Optional.of(ret);
    } else {
      return Optional.absent();
    }
  }

  public static <T extends Comparable<T>> Function<Range<T>, T> lowerEndPointFunction() {
    return new Function<Range<T>, T>() {
      @Override
      public T apply(final Range<T> input) {
        return input.lowerEndpoint();
      }
    };
  }

  public static <T extends Comparable<T>> Function<Range<T>, T> upperEndPointFunction() {
    return new Function<Range<T>, T>() {
      @Override
      public T apply(final Range<T> input) {
        return input.upperEndpoint();
      }
    };
  }
}
