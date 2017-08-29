package com.bbn.bue.common;

import com.google.common.base.Equivalence;
import com.google.common.base.Equivalence.Wrapper;
import com.google.common.base.Function;

/**
 * Utilities for Guava {@link Equivalence}s.
 *
 * @author Ryan Gabbard
 */
public final class EquivalenceUtils {

  private EquivalenceUtils() {
    throw new UnsupportedOperationException();
  }

  public static <T> Function<T, Equivalence.Wrapper<T>> wrapFunction(
      final Equivalence<T> equivalence) {
    return new Function<T, Equivalence.Wrapper<T>>() {
      @Override
      public Equivalence.Wrapper<T> apply(final T item) {
        return equivalence.wrap(item);
      }
    };
  }

  public static <T> Function<Equivalence.Wrapper<T>, T> unwrapFunction() {
    return new Function<Equivalence.Wrapper<T>, T>() {
      @Override
      public T apply(final Equivalence.Wrapper<T> x) {
        return x.get();
      }
    };
  }

  public static <T> Function<Wrapper<T>, String> toStringThroughWrapperFunction() {
    return new Function<Wrapper<T>, String>() {
      @Override
      public String apply(final Wrapper<T> wrapped) {
        return wrapped.get().toString();
      }
    };
  }
}
