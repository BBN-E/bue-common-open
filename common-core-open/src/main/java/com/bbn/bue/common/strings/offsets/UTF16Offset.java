package com.bbn.bue.common.strings.offsets;

/**
 * Character offsets by UTF-16 code units.  If all code points in a string are in the
 * basic multilingual plane, these will be the same as {@link CharOffset}s. However,
 * characters outside the BMP will be encoded as two UTF-16 code units.
 *
 * This is useful because Java stores strings using UTF-16 internally.
 */
public final class UTF16Offset extends AbstractOffset<UTF16Offset> {

  private UTF16Offset(final int val) {
    super(val);
  }

  public static UTF16Offset of(int val) {
    return new UTF16Offset(val);
  }

  @Override
  public String toString() {
    // j for Java
    return "j" + Integer.toString(asInt());
  }

  @Override
  public UTF16Offset shiftedCopy(final int shiftAmount) {
    return UTF16Offset.of(asInt() + shiftAmount);
  }
}
