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
}
