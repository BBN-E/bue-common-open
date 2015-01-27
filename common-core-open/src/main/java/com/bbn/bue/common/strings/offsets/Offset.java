package com.bbn.bue.common.strings.offsets;

public interface Offset {

  /**
   * Prefer {@link #asInt()}
   */
  @Deprecated
  public int value();

  public int asInt();
}
