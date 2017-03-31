package com.bbn.bue.common.strings.offsets;

public interface Offset<SelfType extends Offset<SelfType>> extends Comparable<SelfType> {

  /**
   * Prefer {@link #asInt()}
   */
  @Deprecated
  int value();

  int asInt();

  boolean precedes(SelfType other);

  boolean precedesOrEquals(SelfType other);
  boolean follows(SelfType other);

  boolean followsOrEquals(SelfType other);

  /**
   * Returns another offset of the same type, shifted by the specified distance.
   */
  SelfType shiftedCopy(int shiftAmount);
}
