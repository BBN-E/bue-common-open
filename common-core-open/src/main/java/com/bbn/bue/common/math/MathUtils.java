package com.bbn.bue.common.math;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

/**
 * General mathematical utilities.
 *
 * @author Ryan Gabbard, Jay DeYoung
 */
public final class MathUtils {

  private MathUtils() {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the sum of all elements in the given non-null array {@code arr}.
   */
  public static int sum(int[] arr) {
    int ret = 0;
    for (final int x : arr) {
      ret += x;
    }
    return ret;
  }

  public static Optional<Double> medianOfIntegers(Iterable<Integer> sizes) {
    final ImmutableList<Integer> sorted = Ordering.natural().immutableSortedCopy(sizes);
    if(sorted.size() == 0) {
      return Optional.absent();
    }
    if (sorted.size() % 2 == 0) {
      return Optional.of(0.5 * (sorted.get(sorted.size() / 2) + sorted.get(sorted.size() / 2 - 1)));
    } else {
      return Optional.of((double) sorted.get(sorted.size() / 2));
    }
  }

  public static Optional<Double> medianOfDoubles(Iterable<Double> sizes) {
    final ImmutableList<Double> sorted = Ordering.natural().immutableSortedCopy(sizes);
    if(sorted.size() == 0) {
      return Optional.absent();
    }
    if (sorted.size() % 2 == 0) {
      return Optional.of(0.5 * (sorted.get(sorted.size() / 2) + sorted.get(sorted.size() / 2 - 1)));
    } else {
      return Optional.of(sorted.get(sorted.size() / 2));
    }
  }

  public static Function<Iterable<Double>, Optional<Double>> medianOfDoublesFunction() {
    return new Function<Iterable<Double>, Optional<Double>>() {
      @Override
      public Optional<Double> apply(final Iterable<Double> input) {
        return medianOfDoubles(input);
      }
    };
  }


  /**
   * {@code x * log(x)} when x is non-zero, zero otherwise.
   */
  public static double xLogX(double d) {
    if (d == 0.0) {
      return 0.0;
    } else {
      return d * Math.log(d);
    }
  }

  // deprecated methods

  /**
   * @deprecated This is now in Guava as {@link com.google.common.primitives.Ints#max()}
   */
  @Deprecated
  public static int max(int[] arr) {
    return Ints.max(arr);
  }
}
