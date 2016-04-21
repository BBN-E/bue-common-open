package com.bbn.bue.common;

import com.google.common.collect.ImmutableList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Utilities for working with {@link Finishable}s
 */
public final class Finishables {

  private static final Logger log = LoggerFactory.getLogger(Finishables.class);

  private Finishables() {
    throw new UnsupportedOperationException();
  }

  /**
   * If the provided object is {@link Finishable}, calls {@link Finishable#finish()}. If an
   * exception is thrown, it is logged and not propagated.
   */
  public static void finishIfApplicableAndLogException(Object toFinish) {
    try {
      finishIfApplicable(ImmutableList.of(toFinish));
    } catch (Exception e) {
      log.info("Exception while finishing {}: {}", toFinish, e);
    }
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
   * If the provided object is {@link Finishable}, calls {@link Finishable#finish()}. If an
   * exception is thrown, it is logged and not propagated.
   */
  public static void finishIfApplicableAndLogException(Iterable<?> toFinish) {
    for (final Object o : toFinish) {
      finishIfApplicableAndLogException(o);
    }
  }

  /**
   * Calls {@link Finishable#finish()} on all objects provided
   */
  public static void finish(Iterable<? extends Finishable> toFinish) throws IOException {
    finishIfApplicable(toFinish);
  }

  /**
   * Calls {@link Finishable#finish()} on all objects provided and logs any exceptions thrown rather
   * than propagating them
   */
  public static void finishAndLogException(Iterable<? extends Finishable> toFinish) {
    for (final Finishable o : toFinish) {
      finishIfApplicableAndLogException(o);
    }
  }
}
