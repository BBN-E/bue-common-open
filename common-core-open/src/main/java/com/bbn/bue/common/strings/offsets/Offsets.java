package com.bbn.bue.common.strings.offsets;

import com.google.common.base.Function;

public final class Offsets {

  private Offsets() {
    throw new UnsupportedOperationException();
  }

  public static final <OffsetType extends Offset<OffsetType>> Function<OffsetType, Integer> asIntFunction() {
    return new Function<OffsetType, Integer>() {
      @Override
      public Integer apply(final Offset offset) {
        return offset.asInt();
      }
    };
  }
}
