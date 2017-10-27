package com.bbn.bue.common.collections;

import java.util.Iterator;

/**
 * Utilities for working with {@link java.util.Iterator}s.
 */
public final class IteratorUtils {

  private IteratorUtils() {
    throw new UnsupportedOperationException();
  }

  /**
   * Asserts that that either (a) both provided iterators have more elements or (b) neither does. If
   * not, throws na {@link IllegalStateException}.
   */
  public static void assertStatesMatch(Iterator<?> left, String leftName,
      Iterator<?> right, String rightName, String itemTypePlural) {
    if (left.hasNext() != right.hasNext()) {
      if (left.hasNext()) {
        throw new IllegalStateException(
            leftName + " has more " + itemTypePlural + " than " + rightName);
      } else {
        throw new IllegalStateException(
            rightName + " has more " + itemTypePlural + " than " + leftName);
      }
    }
  }
}
