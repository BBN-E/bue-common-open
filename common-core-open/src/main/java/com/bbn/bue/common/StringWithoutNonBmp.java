package com.bbn.bue.common;

import com.bbn.bue.common.strings.offsets.CharOffset;
import com.bbn.bue.common.strings.offsets.OffsetRange;

import com.google.common.base.Optional;

import org.immutables.value.Value;

/**
 * A {@link UnicodeFriendlyString} which does not contain a non-BMP character. This class
 * should never be referenced directly. Always create {@link UnicodeFriendlyString}s
 * via {@link StringUtils#unicodeFriendly(String)}. See the interface Javadoc for details.
 */
@TextGroupImmutable
@Value.Immutable
abstract class StringWithoutNonBmp extends AbstractUnicodeFriendlyString
    implements UnicodeFriendlyString {

  public abstract String utf16CodeUnits();

  public final boolean hasNonBmpCharacter() {
    return false;
  }

  public boolean hasNonBmpCharacter(OffsetRange<CharOffset> characterRange) {
    return false;
  }

  public static UnicodeFriendlyString of(String utf16CodeUnits) {
    return new StringWithoutNonBmp.Builder().utf16CodeUnits(utf16CodeUnits).build();
  }

  @Override
  public int lengthInUtf16CodeUnits() {
    return utf16CodeUnits().length();
  }

  @Override
  public int lengthInCodePoints() {
    return lengthInUtf16CodeUnits();
  }

  @Override
  public UnicodeFriendlyString substringByCodePoints(final CharOffset startCodepointInclusive) {
    return StringWithoutNonBmp.of(utf16CodeUnits().substring(startCodepointInclusive.asInt()));
  }

  @Override
  public UnicodeFriendlyString substringByCodePoints(final CharOffset startCodepointInclusive,
      final CharOffset endCodepointExclusive) {
    return StringWithoutNonBmp.of(utf16CodeUnits().substring(startCodepointInclusive.asInt(),
        endCodepointExclusive.asInt()));
  }

  @Override
  public boolean isEmpty() {
    return utf16CodeUnits().isEmpty();
  }

  @Override
  public UnicodeFriendlyString trim() {
    return StringWithoutNonBmp.of(utf16CodeUnits().trim());
  }

  @Override
  public final Optional<CharOffset> codePointIndexOf(UnicodeFriendlyString other,
      CharOffset startIndex) {
    final int offset = utf16CodeUnits().indexOf(other.utf16CodeUnits(), startIndex.asInt());
    if (offset >= 0) {
      return Optional.of(CharOffset.asCharOffset(offset));
    } else {
      return Optional.absent();
    }
  }

  public static class Builder extends ImmutableStringWithoutNonBmp.Builder {}
}

