package com.bbn.bue.common.strings.offsets;

public final class ASRTime extends AbstractOffset<ASRTime> {

  /**
   * @deprecated Prefer {@link #of}
   */
  @Deprecated
  public ASRTime(int val) {
    super(val);
  }


  @SuppressWarnings("deprecation")
  public static ASRTime of(int val) {
    return new ASRTime(val);
  }
}
