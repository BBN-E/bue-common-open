package com.bbn.bue.common;

/**
 * Provides a sequence of integers.  The first value returned is guaranteed to be the provided
 * starting value. Each subsequent call will return a value never returned before.  If we run out of
 * unique values, a {@link RuntimeException} will be thrown.
 */
public final class IntIDSequence {

  private int nextValue;
  boolean overflowed = false;

  private IntIDSequence(int nextValue) {
    this.nextValue = nextValue;
  }

  public static IntIDSequence startingFrom(int startVal) {
    return new IntIDSequence(startVal);
  }

  public synchronized int nextID() {
    if (!overflowed) {
      overflowed = (nextValue == Integer.MAX_VALUE);
      return nextValue++;
    } else {
      throw new RuntimeException("ID sequence overflowed");
    }
  }
}
