package com.bbn.bue.common;

/**
 * An integer counter which can be incremented and decremented.  This is handy to use when
 * you would need an integer passed by reference in other languages.
 *
 * @author Ryan Gabbard
 */
public final class IntCounter {

  private int val;

  private IntCounter(final int val) {
    this.val = val;
  }

  public static IntCounter of(int val) {
    return new IntCounter(val);
  }

  public int value() {
    return val;
  }

  public void setValue(int newVal) {
    this.val = newVal;
  }

  public void increment() {
    ++this.val;
  }

  public void decrement() {
    --this.val;
  }

  @Override
  public String toString() {
    return Integer.toString(val);
  }
}
