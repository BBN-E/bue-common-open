package com.bbn.bue.common;

import com.google.common.base.Function;

/**
 * Utility methods for working with Class objects
 */
public final class ClassUtils {

  private ClassUtils() {
    throw new UnsupportedOperationException();
  }

  /**
   * A Guava {@link Function} to convert classes to their names. Does not accept {@code null}.
   */
  public static Function<Class, String> nameFunction() {
    return ClassUtils.ClassNameFunction.INSTANCE;
  }

  private enum ClassNameFunction implements Function<Class, String> {
    INSTANCE;

    @Override
    public String apply(final Class input) {
      return input.getName();
    }
  }

  /**
   * A {@link Function} mapping objects to their {@link Class}. Does not accept {@code null}.
   */
  public static Function<Object, Class<?>> classFunction() {
    return ClassFunction.INSTANCE;
  }

  private enum ClassFunction implements Function<Object, Class<?>> {
    INSTANCE;

    @Override
    public Class<?> apply(final Object input) {
      return input.getClass();
    }
  }
}
