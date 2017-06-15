package com.bbn.bue.common;

import com.bbn.bue.common.strings.offsets.CharOffset;
import com.bbn.bue.common.strings.offsets.OffsetRange;
import com.bbn.bue.common.strings.offsets.UTF16Offset;

import com.google.common.base.Optional;

import org.immutables.func.Functional;
import org.immutables.value.Value;

import static com.bbn.bue.common.strings.offsets.CharOffset.asCharOffset;

/**
 * A {@link UnicodeFriendlyString} which actually contains a non-BMP character. This class
 * should never be referenced directly in user code because it assumes the provided string
 * has a non-BMP character. {@link UnicodeFriendlyString}s should always be created via
 * {@link StringUtils#unicodeFriendly(String)}.
 *
 * See the interface Javadoc for details.  Currently this uses simple but slow
 * implementations for most of its method, but much faster versions a can be added when needed
 * by using a structure similar to {@link com.bbn.bue.common.strings.LocatedString}'s
 * {@link com.bbn.bue.common.strings.LocatedString.CharacterRegion}.
 */
@TextGroupImmutable
@Value.Immutable
@Functional
abstract class StringWithNonBmp extends AbstractUnicodeFriendlyString
    implements UnicodeFriendlyString {

  public abstract String utf16CodeUnits();

  public final boolean hasNonBmpCharacter() {
    return true;
  }

  public boolean hasNonBmpCharacter(OffsetRange<CharOffset> characterRange) {
    // slow placeholder implementation
    for (int i = codeUnitOffsetFor(characterRange.startInclusive()).asInt();
         i <= codeUnitOffsetFor(characterRange.endInclusive()).asInt(); ) {
      final int codePoint = utf16CodeUnits().codePointAt(i);
      final int charsForCodePoint = Character.charCount(codePoint);
      if (charsForCodePoint > 1) {
        return true;
      } else {
        i += charsForCodePoint;
      }
    }
    return false;
  }

  @Value.Derived
  public int lengthInUtf16CodeUnits() {
    return utf16CodeUnits().length();
  }

  @Value.Derived
  public int lengthInCodePoints() {
    return utf16CodeUnits().codePointCount(0, utf16CodeUnits().length());
  }

  @Override
  public int codepointAtCodepointIndex(final CharOffset codepointIdx) {
    return utf16CodeUnits().codePointAt(codeUnitOffsetFor(codepointIdx).asInt());
  }

  static UnicodeFriendlyString of(String s) {
    return new Builder().utf16CodeUnits(s).build();
  }



  public final UnicodeFriendlyString substringByCodePoints(CharOffset startCodepointInclusive) {
    return substringByCodePoints(startCodepointInclusive,
        asCharOffset(lengthInCodePoints()));
  }

  public final UnicodeFriendlyString substringByCodePoints(CharOffset startCodepointInclusive,
      CharOffset endCodepointExclusive) {
    // this is a slow, simple, temporary implementation
    final UTF16Offset startCodeUnitInclusive = codeUnitOffsetFor(startCodepointInclusive);
    final UTF16Offset endCodeUnitExclusive = codeUnitOffsetFor(endCodepointExclusive);

    // we need this because in the current implementation we can't tell if our substring
    // includes no non-BMP characters and should therefore use the other implementation
    return StringUtils.unicodeFriendly(utf16CodeUnits().substring(startCodeUnitInclusive.asInt(),
        endCodeUnitExclusive.asInt()));
  }

  // this is a slow temporary implementation. If performance becomes important, we can store
  // something like LocatedString's like CharacterRegions
  private UTF16Offset codeUnitOffsetFor(final CharOffset codePointOffset) {
    int charOffset = 0;
    int codePointsConsumed = 0;

    for (; charOffset < utf16CodeUnits().length() && codePointsConsumed < codePointOffset.asInt();
         ++codePointsConsumed) {
      final int codePoint = utf16CodeUnits().codePointAt(charOffset);
      charOffset += Character.charCount(codePoint);
    }
    if (codePointsConsumed == codePointOffset.asInt()) {
      return UTF16Offset.of(charOffset);
    } else {
      // this will happen if codePointOffset is negative or equal to or greater than the
      // total number of codepoints in the string
      throw new IndexOutOfBoundsException();
    }
  }

  private CharOffset charOffsetFor(UTF16Offset offset) {
    // slow placeholder implementation
    int charOffset = 0;
    int codePointsConsumed = 0;

    while(charOffset < utf16CodeUnits().length() && codePointsConsumed < offset.asInt()) {
      final int codePoint = utf16CodeUnits().codePointAt(codePointsConsumed);
      charOffset++;
      codePointsConsumed += Character.charCount(codePoint);
    }

    if (codePointsConsumed == offset.asInt()) {
      return asCharOffset(charOffset);
    } else {
      // this will happen if codePointOffset is negative or equal to or greater than the
      // total number of codepoints in the string
      throw new IndexOutOfBoundsException();
    }
  }


  public boolean isEmpty() {
    return utf16CodeUnits().isEmpty();
  }

  public final UnicodeFriendlyString trim() {
    return StringWithNonBmp.of(utf16CodeUnits().trim());
  }

  @Override
  public final Optional<CharOffset> codePointIndexOf(UnicodeFriendlyString other,
      CharOffset startIndex) {
    if (startIndex.asInt() < 0 || startIndex.asInt() > lengthInCodePoints()) {
      throw new IndexOutOfBoundsException("StartIndex was out of bounds: " + startIndex);
    }
    final UTF16Offset offsetForStart = codeUnitOffsetFor(startIndex);
    final int matchingOffset =
        utf16CodeUnits().indexOf(other.utf16CodeUnits(), offsetForStart.asInt());
    if (matchingOffset < 0) {
      return Optional.absent();
    } else {
      final UTF16Offset utf16Offset = UTF16Offset.of(matchingOffset);
      final CharOffset charOffset = charOffsetFor(utf16Offset);
      return Optional.of(charOffset);
    }
  }

  @Override
  public final <T> void processCodePoints(CodePointProcessor<T> codePointProcessor) {
    for (int codeUnitOffset = 0, codePointOffset = 0; codeUnitOffset < utf16CodeUnits().length();
         ++codePointOffset) {
      final int codePoint = utf16CodeUnits().codePointAt(codePointOffset);
      codePointProcessor.processCodepoint(this, asCharOffset(codePointOffset), codePoint);
      codeUnitOffset += Character.charCount(codePoint);
    }
  }

  @Override
  public final String toString() {
    return super.toString();
  }

  @Override
  public final int hashCode() {
    return super.hashCode();
  }

  @Override
  public final boolean equals(Object o) {
    return super.equals(o);
  }

  static class Builder extends ImmutableStringWithNonBmp.Builder {

  }
}
