package com.bbn.bue.common;

import java.io.IOException;

/**
 * Utilities for working with {@link Finishable}s
 */
public final class Finishables {
  private Finishables() {
    throw new UnsupportedOperationException();
  }

  /**
   * Calls {@link Finishable#finish()} on all objects provided which implement {@link Finishable}
   */
  public static void finishIfApplicable(Iterable<?> toFinish) throws IOException {
    for (final Object o : toFinish) {
      if (o instanceof Finishable) {
        ((Finishable) o).finish();
      }
    }
  }

  /**
   * Calls {@link Finishable#finish()} on all objects provided
   */
  public static void finish(Iterable<? extends Finishable> toFinish) throws IOException {
    finishIfApplicable(toFinish);
  }
}
