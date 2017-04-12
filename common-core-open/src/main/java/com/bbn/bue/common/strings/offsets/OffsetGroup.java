package com.bbn.bue.common.strings.offsets;

import com.bbn.bue.common.TextGroupImmutable;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;

import org.immutables.func.Functional;
import org.immutables.value.Value;

import java.util.ArrayList;
import java.util.List;

@TextGroupImmutable
@Value.Immutable
@Functional
public abstract class OffsetGroup {

  public abstract Optional<ByteOffset> byteOffset();

  public abstract CharOffset charOffset();

  public abstract EDTOffset edtOffset();

  public abstract Optional<ASRTime> asrTime();

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
    return new Builder().charOffset(charOffset).edtOffset(edtOffset).build();
  }

  public static OffsetGroup from(final ByteOffset byteOffset, final CharOffset charOffset,
      final EDTOffset edtOffset) {
    return new Builder().byteOffset(byteOffset).charOffset(charOffset)
        .edtOffset(edtOffset).build();
  }

  /**
   * Returns whether, for all offsets present in both groups, this {@code OffsetGroup}'s
   * offset {@link Offset#precedesOrEquals(Offset)} the other's.
   */
  public boolean precedesOrEqualsForAllOffsetTypesInBoth(final OffsetGroup other) {
    return charOffset().precedesOrEquals(other.charOffset())
        && edtOffset().precedesOrEquals(other.edtOffset())
        && (!byteOffset().isPresent() || !other.byteOffset().isPresent()
                || byteOffset().get().precedesOrEquals(other.byteOffset().get()))
        && (!asrTime().isPresent() || !other.asrTime().isPresent()
                || asrTime().get().precedesOrEquals(other.asrTime().get()));
  }

  /**
   * Returns whether, for all offsets present in both groups, this {@code OffsetGroup}'s
   * offset {@link Offset#precedes(Offset)} (Offset)} the other's.
   */
  public boolean precedesForAllOffsetTypesInBoth(final OffsetGroup other) {
    return charOffset().precedes(other.charOffset())
        && edtOffset().precedes(other.edtOffset())
        && (!byteOffset().isPresent() || !other.byteOffset().isPresent()
                || byteOffset().get().precedes(other.byteOffset().get()))
        && (!asrTime().isPresent() || !other.asrTime().isPresent()
                || asrTime().get().precedes(other.asrTime().get()));
  }

  /**
   * Returns whether, for all offsets present in both groups, this {@code OffsetGroup}'s
   * offset {@link Offset#followsOrEquals(Offset)} (Offset)} the other's.
   */
  public boolean followsOrEqualsForAllOffsetTypesInBoth(final OffsetGroup other) {
    return charOffset().followsOrEquals(other.charOffset())
        && edtOffset().followsOrEquals(other.edtOffset())
        && (!byteOffset().isPresent() || !other.byteOffset().isPresent()
                || byteOffset().get().followsOrEquals(other.byteOffset().get()))
        && (!asrTime().isPresent() || !other.asrTime().isPresent()
                || asrTime().get().followsOrEquals(other.asrTime().get()));
  }

  /**
   * Returns whether, for all offsets present in both groups, this {@code OffsetGroup}'s
   * offset {@link Offset#follows(Offset)} (Offset)} the other's.
   */
  public boolean followsForAllOffsetTypesInBoth(final OffsetGroup other) {
    return charOffset().follows(other.charOffset())
        && edtOffset().follows(other.edtOffset())
        && (!byteOffset().isPresent() || !other.byteOffset().isPresent()
                || byteOffset().get().follows(other.byteOffset().get()))
        && (!asrTime().isPresent() || !other.asrTime().isPresent()
                || asrTime().get().follows(other.asrTime().get()));
  }

  public static final Joiner SEMICOLON_JOINER = Joiner.on(";");
  @Override
  public String toString() {
    final List<String> parts = new ArrayList<>();

    // frequently the char, EDT, and UTF-16 offsets are all the same. For compactness,
    // when this is the case we only print the offset number once, using all
    // three prefixes
    final StringBuilder primaryPartPrefix = new StringBuilder("c");
    final boolean charOffsetsEqualEdtOffsets = charOffset().asInt() == edtOffset().asInt();
    if (charOffsetsEqualEdtOffsets) {
      primaryPartPrefix.append("e");
    }

    parts.add(primaryPartPrefix.toString() + Integer.toString(charOffset().asInt()));

    // then add EDT offsets if not equal to the char offset
    if (!charOffsetsEqualEdtOffsets) {
      parts.add(edtOffset().toString());
    }

    // then add all other offsets, if present
    if (byteOffset().isPresent()) {
      parts.add(byteOffset().get().toString());
    }

    if (asrTime().isPresent()) {
      parts.add(asrTime().get().toString());
    }

    return SEMICOLON_JOINER.join(parts);
  }

  public static class Builder extends ImmutableOffsetGroup.Builder {

  }

}
