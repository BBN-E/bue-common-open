package com.bbn.bue.common.strings.offsets;

public final class EDTOffset extends AbstractOffset<EDTOffset> {

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
  public EDTOffset shiftedCopy(final int shiftAmount) {
    return asEDTOffset(asInt()+shiftAmount);
  }
}
