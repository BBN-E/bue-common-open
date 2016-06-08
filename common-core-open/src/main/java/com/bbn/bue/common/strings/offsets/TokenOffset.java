package com.bbn.bue.common.strings.offsets;

/**
 * Represents an offset into a token sequence (whether sentence- or document-based)
 */
public final class TokenOffset extends AbstractOffset<TokenOffset> {

  private TokenOffset(final int val) {
    super(val);
  }

  public static TokenOffset asTokenOffset(final int val) {
    return new TokenOffset(val);
  }

  @Override
  public String toString() {
    return "t" + Integer.toString(asInt());
  }

  @Override
  public TokenOffset shiftedCopy(final int shiftAmount) {
    return TokenOffset.asTokenOffset(asInt() + shiftAmount);
  }
}
