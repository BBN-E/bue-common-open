package com.bbn.bue.common.strings.offsets;

import com.google.common.base.Objects;
import com.google.common.base.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class OffsetGroup {

  private OffsetGroup(final ByteOffset byteOffset, final CharOffset charOffset,
      final EDTOffset edtOffset, final ASRTime asrTime) {
    this.byteOffset = byteOffset;
    this.charOffset = checkNotNull(charOffset);
    this.edtOffset = checkNotNull(edtOffset);
    this.asrTime = asrTime;
  }

  /**
   * Prefer static factory method
   * @param byteOffset
   * @param charOffset
   * @param edtOffset
   * @param asrTime
   */
/*	@Deprecated
        public OffsetGroup(final Optional<ByteOffset> byteOffset, final CharOffset charOffset, final EDTOffset edtOffset, final Optional<ASRTime> asrTime) {
		this.byteOffset = checkNotNull(byteOffset).orNull();
		this.charOffset = checkNotNull(charOffset);
		this.edtOffset = checkNotNull(edtOffset);
		this.asrTime = checkNotNull(asrTime).orNull();
	}*/

  /**
   * Creates an offset group using the same offset value as both the {@link
   * com.bbn.bue.common.strings.offsets.CharOffset} and {@link com.bbn.bue.common.strings.offsets.EDTOffset}.
   * This is for unit testing purposes and probably shouldn't be used in other code.
   */
  public static OffsetGroup fromMatchingCharAndEDT(int offset) {
    return from(CharOffset.asCharOffset(offset), EDTOffset.asEDTOffset(offset));
  }

  /**
   * Creates a {@code OffsetGroup} with the specified character and EDT offsets and no specified
   * byte or ASR offsets.
   */
  public static OffsetGroup from(final CharOffset charOffset, final EDTOffset edtOffset) {
    return new OffsetGroup(null, charOffset, edtOffset, null);
  }

  public static OffsetGroup from(final ByteOffset byteOffset, final CharOffset charOffset,
      final EDTOffset edtOffset) {
    return new OffsetGroup(byteOffset, charOffset, edtOffset, null);
  }

  private final ByteOffset byteOffset;
  private final CharOffset charOffset;
  private final EDTOffset edtOffset;
  private final ASRTime asrTime;

  public Optional<ByteOffset> byteOffset() {
    return Optional.fromNullable(byteOffset);
  }

  public CharOffset charOffset() {
    return charOffset;
  }

  public EDTOffset edtOffset() {
    return edtOffset;
  }

  public Optional<ASRTime> asrTime() {
    return Optional.fromNullable(asrTime);
  }

  @Override
  public boolean equals(final Object other) {
    if (other == null) {
      return false;
    }
    if (other.getClass() != getClass()) {
      return false;
    }

    final OffsetGroup ogOther = (OffsetGroup) other;
    return Objects.equal(byteOffset, ogOther.byteOffset) &&
        Objects.equal(charOffset, ogOther.charOffset) &&
        Objects.equal(edtOffset, ogOther.edtOffset) &&
        Objects.equal(asrTime, ogOther.asrTime);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(byteOffset, charOffset, edtOffset, asrTime);
  }

  @Override
  public String toString() {
    final StringBuilder ret = new StringBuilder();

    ret.append("OffsetGroup[");
    ret.append("char=").append(charOffset.value())
        .append(";edt=").append(edtOffset.value());

    if (byteOffset != null) {
      ret.append(";byte=").append(byteOffset.value());
    }

    if (asrTime != null) {
      ret.append(";asr=").append(asrTime.value());
    }

    ret.append("]");
    return ret.toString();
  }
}
