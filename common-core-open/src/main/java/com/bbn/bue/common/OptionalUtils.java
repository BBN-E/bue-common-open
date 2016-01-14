package com.bbn.bue.common;

import com.google.common.base.Function;
import com.google.common.base.Optional;

/**
 * Utilities for working with {@link com.google.common.base.Optional}
 */
public final class OptionalUtils {

  private OptionalUtils() {
    throw new UnsupportedOperationException();
  }

  /**
   * Provides a function to apply {@link Optional#or(Object)} to any input {@code Optional}
   */
  public static <T> Function<Optional<T>, T> deoptionalizeFunction(final T replaceAbsentWith) {
    return new Function<Optional<T>, T>() {
      @Override
      public T apply(final Optional<T> input) {
        return input.or(replaceAbsentWith);
      }
    };
  }

  /**
   * Throws an {@link IllegalStateException} if both passed {@link Optional}s are present or neither is.
   */
  public static void exactlyOnePresentOrIllegalState(final Optional<?> x,
      final Optional<?> y, final String msg) {
    if (x.isPresent() == y.isPresent()) {
      throw new IllegalStateException(msg);
    }
  }

  /**
   * Throws an {@link IllegalArgumentException} if both passed {@link Optional}s are present or neither is.
   */
  public static void exactlyOnePresentOrIllegalArgument(final Optional<?> x,
      final Optional<?> y, final String msg) {
    if (x.isPresent() == y.isPresent()) {
      throw new IllegalArgumentException(msg);
    }
  }
}
