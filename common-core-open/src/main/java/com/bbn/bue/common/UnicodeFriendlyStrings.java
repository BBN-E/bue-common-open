package com.bbn.bue.common;

import com.google.common.base.Function;

/**
 * Utiltiy methods for {@link UnicodeFriendlyString}
 */
public class UnicodeFriendlyStrings {
  private UnicodeFriendlyStrings() {
    throw new UnsupportedOperationException();
  }

  public static Function<UnicodeFriendlyString, Integer> lengthInCodePointsFunction() {
    return LengthInCodePointsFunction.INSTANCE;
  }

  private enum LengthInCodePointsFunction implements Function<UnicodeFriendlyString, Integer> {
    INSTANCE;

    @Override
    public Integer apply(final UnicodeFriendlyString ufs) {
      return ufs.lengthInCodePoints();
    }

    @Override
    public String toString() {
      return "lengthInCodepoints()";
    }
  }
}
