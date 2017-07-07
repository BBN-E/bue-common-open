package com.bbn.bue.common;

import com.bbn.bue.common.strings.offsets.CharOffset;
import com.bbn.bue.common.strings.offsets.OffsetRange;
import com.bbn.bue.common.strings.offsets.UTF16Offset;

import com.google.common.base.Optional;

/**
 * A wrapper for {@link String} which makes it easier (and more efficient) not to make errors
 * in the presence of Unicode codepoints outside the Basic Multilingual Plane (BMP).
 *
 * Java internally uses a UTF-16 String representations. In this encoding, the vast majority
 * of commonly used Unicode codepoints are represented by a single {@code char}.  However,
 * there are many Unicode codepoints which require two {@code char}s (perhaps most importantly
 * emojis in Twitter and discussion forum texts).  These multi-char characters will cause
 * discrepancies between Java {@code char} offsets and lengths and Unicode codepoint offsets
 * and length.  Unfortunately, many of our evaluations require us to work with Unicode codepoint
 * offsets and the presence of multi-char characters can lead to incorrect offsets throughout
 * a document when used with naive Java code. Using this class to wrap all strings coming from
 * documents can help prevent such errors.
 *
 * Do not attempt to create a {@code UnicodeFriendlyString} directly. Instead apply
 * {@link StringUtils#unicodeFriendly(String)} to a Java {@link String}.
 *
 * Equality between {@code UnicodeFriendlyString}s is determined by equality of
 * {@link #utf16CodeUnits()}.  {@link #hashCode()}  is defined to be the hash code of the
 * {@link #utf16CodeUnits()}.
 */
public interface UnicodeFriendlyString {

  /**
   * Does this string actually contain a character outside the BMP?
   */
  boolean hasNonBmpCharacter();

  /**
   * Are any of the characters in the specified range of code point offsets in this string
   * outside the BMP?
   */
  boolean hasNonBmpCharacter(OffsetRange<CharOffset> codepointOffsetRange);

  /**
   * The representation of this String in UTF-16, Java's native form
   */
  String utf16CodeUnits();

  /**
   * The size of the representation of this string in UTF-16, Java's native form.
   * This will equal {@link #utf16CodeUnits()}'s {@link String#length()}.
   */
  int lengthInUtf16CodeUnits();

  /**
   * The number of Unicode code points in this string.  This will differ from
   * {@link #lengthInUtf16CodeUnits()} if and only if non-BMP characters are present.
   */
  int lengthInCodePoints();

  /**
   * Returns the Unicode codepoint at the given codepoint index.
   */
  int codepointAtCodepointIndex(CharOffset codepointIdx);

  /**
   * Tests if this string starts with the specified prefix.  Always returns {@code true} for
   * if the argument is the empty string.
   */
  boolean startsWith(UnicodeFriendlyString ufs);

  /**
   * Tests if the substring of this string beginning at the specified index starts with the
   * specified prefix. Always returns {@code true} for if the argument is the empty string.
   */
  boolean startsWith(UnicodeFriendlyString ufs, CharOffset offset);

  /**
   * Returns the substring, in terms of code point offsets,
   * starting at {@code startCodepointInclusive} and continuing to the end of the string.
   */
  UnicodeFriendlyString substringByCodePoints(CharOffset startCodepointInclusive);

  /**
   * Returns the substring, in terms of code point offsets,
   * starting at {@code startCodepointInclusive} and continuing up to (but not including)
   * {@code endCodepointExclusive}.
   */
  UnicodeFriendlyString substringByCodePoints(CharOffset startCodepointInclusive,
      CharOffset endCodepointExclusive);

  /**
   * Returns the substring, in terms of code point offsets,
   * covering the specified (inclusive) range of code points.
   */
  UnicodeFriendlyString substringByCodePoints(OffsetRange<CharOffset> codePointRange);

  boolean isEmpty();

  /**
   * Returns a copy of this string with whitespace trimmed from either end.
   */
  UnicodeFriendlyString trim();

  boolean contains(UnicodeFriendlyString other);

  boolean contains(String otherCodeUnits);

  /**
   * Returns the {@link CharOffset} for the first occurrence of {@code other} within this
   * {@link UnicodeFriendlyString}. If {@code other} does not occur as a substring,
   * {@link Optional#absent()} is returned. Plain {@link String} codePointIndexOf() methods are not
   * supported to restrict users from accidentally calling a substring with a naive
   * {@link String#length()} as that length will be in UTF16 offsets and not behave correctly with
   * this interface when used with non-BMP characters.
   */
  Optional<CharOffset> codePointIndexOf(UnicodeFriendlyString other);

  /**
   * Returns the {@link CharOffset} for the first occurrence of {@code other} within this
   * {@link UnicodeFriendlyString} which beings at or after {@code startIndex}. If {@code other}
   * does not occur as a substring, {@link Optional#absent()}  is returned.  Plain {@link String}
   * codePointIndexOf() methods are not supported to restrict users from accidentally calling a
   * substring with a naive {@link String#length()} as that length will be in UTF16 offsets and not
   * behave correctly with this interface when used with non-BMP characters.
   */
  Optional<CharOffset> codePointIndexOf(UnicodeFriendlyString other, CharOffset startIndex);

  /**
   * Process each codepoint of this string with a {@link CodePointProcessor}.  This is how
   * you should replace things which would be for loops over the indices of a regular {@link String}.
   */
  <T> void processCodePoints(CodePointProcessor<T> codePointProcessor);

  /**
   * Gets the codepoint index of a code unit offset
   */
  CharOffset codepointIndex(UTF16Offset offset);

  /**
   * Something which can process the code points of a string and produce some result.  Note that
   * in many cases it is easier to create an anonymous {@link NoResultCodePointProcessor} and
   * ignore {@link #getResult()}.
   */
  interface CodePointProcessor<T> {
    void processCodepoint(UnicodeFriendlyString s, CharOffset codePointOffset, int codePoint);
    T getResult();
  }

  /**
   * A {@link CodePointProcessor} useful for making anonymous {@code CodePointProcessors} which
   * feed their output directly into local variables in a method and doesn't bother with
   * {@link #getResult()}.
   */
  abstract class NoResultCodePointProcessor implements CodePointProcessor<Void> {
    @Override
    public final Void getResult() {
      throw new UnsupportedOperationException("Should not call getResult on "
          + "a NoResultCodePointProcessor");
    }
  }

}
