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

  /**
   * Orders Ts in some domain by their image under F using the onFunctionResultOrdering
   */
  public static <T,V> Ordering<T> onResultOf(final Function<T, V> F, final Ordering<V> onFunctionResult) {
    return new Ordering<T>() {
      @Override
      public int compare(final T t, final T t1) {
        return onFunctionResult.compare(F.apply(t), F.apply(t1));
      }
    };
  }
}
