package com.bbn.bue.common.strings.offsets;

public final class CharOffset extends AbstractOffset<CharOffset> {

  /**
   * @deprecated Prefer {@link #asCharOffset(int)}
   */
  public CharOffset(final int val) {
    super(val);
  }

  @SuppressWarnings("deprecation")
  public static CharOffset asCharOffset(final int val) {
    return new CharOffset(val);
  }

  @Override
  public String toString() {
    return "c" + Integer.toString(asInt());
  }
}
