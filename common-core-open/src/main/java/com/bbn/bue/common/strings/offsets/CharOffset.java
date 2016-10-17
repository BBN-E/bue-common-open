package com.bbn.bue.common.strings.offsets;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class CharOffset extends AbstractOffset<CharOffset> {

  /**
   * @deprecated Prefer {@link #asCharOffset(int)}
   */
  public CharOffset(final int val) {
    super(val);
  }

  @SuppressWarnings("deprecation")
  @JsonCreator
  public static CharOffset asCharOffset(@JsonProperty("value") final int val) {
    return new CharOffset(val);
  }

  @Override
  public String toString() {
    return "c" + Integer.toString(asInt());
  }

  @Override
  public CharOffset shiftedCopy(final int shiftAmount) {
    return CharOffset.asCharOffset(asInt() + shiftAmount);
  }
}

