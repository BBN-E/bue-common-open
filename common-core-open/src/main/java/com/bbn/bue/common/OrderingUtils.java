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
   * equal.  Any explicitly ranked item comes before than any non-explicitly ranked item
   * in the ordering.
   */
  @SafeVarargs
  public static <T> Ordering<T> explicitOrderingUnrankedLast(T leastRankedValue,
      T... remainingValuesInOrder) {
    return explicitOrderingUnrankedLast(
        Lists.asList(leastRankedValue, remainingValuesInOrder));
  }

  /**
   * Like {@link Ordering#explicit(List)} but does not throw an exception when comparing
   * items not explicitly ranked.  Instead, two items not explicitly ranked are considered
   * equal.  Any explicitly ranked item comes before any non-explicitly ranked item in the ordering.
   */
  public static <T> Ordering<T> explicitOrderingUnrankedLast(
      final List<T> valuesInOrder) {
    return ImmutableExplicitOrderingNonExclusive.<T>builder()
        .rankMap(MapUtils.indexMap(valuesInOrder))
        .unrankedIsFirst(false)
        .build();
  }

  /**
   * Like {@link Ordering#explicit(Object, Object[])} (List)} but does not throw an exception when
   * comparing items not explicitly ranked.  Instead, two items not explicitly ranked are considered
   * equal.  Any explicitly ranked item is comes after any non-explicitly ranked item
   * in the ordering.
   */
  @SafeVarargs
  public static <T> Ordering<T> explicitOrderingUnrankedFirst(T leastRankedValue,
      T... remainingValuesInOrder) {
    return explicitOrderingUnrankedFirst(
        Lists.asList(leastRankedValue, remainingValuesInOrder));
  }

  /**
   * Like {@link Ordering#explicit(List)} but does not throw an exception when comparing
   * items not explicitly ranked.  Instead, two items not explicitly ranked are considered
   * equal.  Any explicitly ranked item comes after than any non-explicitly ranked item
   * in the ordering.
   */
  public static <T> Ordering<T> explicitOrderingUnrankedFirst(
      final List<T> valuesInOrder) {
    return ImmutableExplicitOrderingNonExclusive.<T>builder()
        .rankMap(MapUtils.indexMap(valuesInOrder))
        .unrankedIsFirst(true)
        .build();
  }
}


