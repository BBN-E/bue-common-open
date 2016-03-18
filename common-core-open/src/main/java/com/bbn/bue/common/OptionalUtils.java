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
   * Guava {@link Function} to call {@link Optional#get()}; this will throw an {@link
   * IllegalStateException} if the input is {@link Optional#absent()}.
   */
  public static <T> Function<Optional<T>, T> getFunction() {
    return new Function<Optional<T>, T>() {
      @Override
      public T apply(final Optional<T> input) {
        return input.get();
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
   * Throws an {@link IllegalStateException} if it is not true that exactly one of the provided
   * {@link Optional}s {@link Optional#isPresent()}.
   */
  public static <T> void exactlyOnePresentOrIllegalState(
      Iterable<? extends Optional<? extends T>> optionals, final String msg) {
    if (numPresent(optionals) != 1) {
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

  /**
   * Throws an {@link IllegalArgumentException} if it is not true that exactly one of the provided
   * {@link Optional}s {@link Optional#isPresent()}.
   */
  public static <T> void exactlyOnePresentOrIllegalArgument(
      Iterable<? extends Optional<? extends T>> optionals, final String msg) {
    if (numPresent(optionals) != 1) {
      throw new IllegalArgumentException(msg);
    }
  }

  /**
   * Returns the number of provided {@link Optional}s which are {@link Optional#isPresent()}.
   */
  public static <T> int numPresent(Iterable<? extends Optional<? extends T>> optionals) {
    int numPresent = 0;
    for (final Optional<?> optional : optionals) {
      if (optional.isPresent()) {
        ++numPresent;
      }
    }
    return numPresent;
  }
}
