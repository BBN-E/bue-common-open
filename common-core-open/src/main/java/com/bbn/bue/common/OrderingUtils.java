package com.bbn.bue.common;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;

public final class OrderingUtils {

  private OrderingUtils() {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a function which maps any iterable to its minimum according to the supplied {@link
   * com.google.common.collect.Ordering}.  If no such minimum exists for an input, it will throw an
   * exception as specified in {@link Ordering#min(Iterable)}.
   */
  public static <T> Function<Iterable<T>, T> minFunction(final Ordering<T> ordering) {
    return new Function<Iterable<T>, T>() {
      @Override
      public T apply(Iterable<T> input) {
        return ordering.min(input);
      }
    };
  }

  /**
   * Gets a function which maps any iterable to its maximum according to the supplied {@link
   * com.google.common.collect.Ordering}.  If no such maximum exists for an input, it will throw an
   * exception as specified in {@link Ordering#max(Iterable)}.
   */
  public static <T> Function<Iterable<T>, T> maxFunction(final Ordering<T> ordering) {
    return new Function<Iterable<T>, T>() {
      @Override
      public T apply(Iterable<T> input) {
        return ordering.max(input);
      }
    };
  }
}
