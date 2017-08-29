package com.bbn.bue.common.hppc;

import com.carrotsearch.hppc.ObjectCollection;

/**
 * Utilities for working with the high-performance primitive collections library
 */
public final class HppcUtils {

  private HppcUtils() {
    throw new UnsupportedOperationException();
  }

  /**
   * HPPC Iterables are funny because they return cursor objects for performance. But sometimes you
   * need just a plain Java {@link Iterable}. This will do the conversion.
   */
  public static <T> Iterable<T> asJavaIterable(ObjectCollection<T> objectCollection) {
    return new ObjectCollectionAsIterable<>(objectCollection);
  }
}
