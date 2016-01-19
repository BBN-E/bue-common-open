package com.bbn.bue.common.strings.offsets;

public final class ByteOffset extends AbstractOffset<ByteOffset> {

  /**
   * @deprecated Prefer {@link #asByteOffset(int)}.
   */
  @Deprecated
  public ByteOffset(int val) {
    super(val);
  }

  @SuppressWarnings("deprecation")
  public static ByteOffset asByteOffset(final int val) {
    return new ByteOffset(val);
  }

  @Override
  public String toString() {
    return "b" + asInt();
  }
}
