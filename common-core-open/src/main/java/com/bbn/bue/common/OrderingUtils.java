package com.bbn.bue.common;

import com.bbn.bue.common.collections.MapUtils;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import java.util.List;

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
   * Like {@link Ordering#explicit(Object, Object[])} but does not throw an exception when comparing
   * items not explicitly ranked.  Instead, two items not explicitly ranked are considered
   * equal.  Any explicitly ranked item is considered smaller than any non-explicitly ranked item.
   */
  @SafeVarargs
  public static <T> Ordering<T> explicitOrderingNonExclusiveUnrankedSmaller(T leastRankedValue,
      T... remainingValuesInOrder) {
    return explicitOrderingNonExclusiveUnrankedSmaller(
        Lists.asList(leastRankedValue, remainingValuesInOrder));
  }

  /**
   * Like {@link Ordering#explicit(List)} but does not throw an exception when comparing
   * items not explicitly ranked.  Instead, two items not explicitly ranked are considered
   * equal.  Any explicitly ranked item is considered smaller than any non-explicitly ranked item.
   */
  public static <T> Ordering<T> explicitOrderingNonExclusiveUnrankedSmaller(
      final List<T> valuesInOrder) {
    return ImmutableExplicitOrderingNonExclusive.<T>builder()
        .rankMap(MapUtils.indexMap(valuesInOrder))
        .unrankedIsLarger(false)
        .build();
  }

  /**
   * Like {@link Ordering#explicit(Object, Object[])} (List)} but does not throw an exception when
   * comparing items not explicitly ranked.  Instead, two items not explicitly ranked are considered
   * equal.  Any explicitly ranked item is considered larger than any non-explicitly ranked item.
   */
  @SafeVarargs
  public static <T> Ordering<T> explicitOrderingNonExclusiveUnrankedLarger(T leastRankedValue,
      T... remainingValuesInOrder) {
    return explicitOrderingNonExclusiveUnrankedLarger(
        Lists.asList(leastRankedValue, remainingValuesInOrder));
  }

  /**
   * Like {@link Ordering#explicit(List)} but does not throw an exception when comparing
   * items not explicitly ranked.  Instead, two items not explicitly ranked are considered
   * equal.  Any explicitly ranked item is considered larger than any non-explicitly ranked item.
   */
  public static <T> Ordering<T> explicitOrderingNonExclusiveUnrankedLarger(
      final List<T> valuesInOrder) {
    return ImmutableExplicitOrderingNonExclusive.<T>builder()
        .rankMap(MapUtils.indexMap(valuesInOrder))
        .unrankedIsLarger(true)
        .build();
  }
}


