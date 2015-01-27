package com.bbn.bue.common.strings.offsets;

import com.google.common.primitives.Ints;

public final class EDTOffset extends AbstractOffset implements Comparable<EDTOffset> {

  /**
   * Deprecated as public constructor, use asEDTOffset
   */
  @Deprecated
  public EDTOffset(int val) {
    super(val);
  }

  public static EDTOffset asEDTOffset(int val) {
    return new EDTOffset(val);
  }

  @Override
  public int compareTo(EDTOffset o) {
    return Ints.compare(value(), o.value());
  }

  @Override
  public boolean equals(final Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
