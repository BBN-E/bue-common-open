package com.bbn.bue.common.strings.offsets;

public interface Offset<SelfType extends Offset<SelfType>> extends Comparable<SelfType> {

  /**
   * Prefer {@link #asInt()}
   */
  @Deprecated
  int value();

  int asInt();

  /**
   * Returns another offset of the same type, shifted by the specified distance.
   */
  SelfType shiftedCopy(int shiftAmount);
}
