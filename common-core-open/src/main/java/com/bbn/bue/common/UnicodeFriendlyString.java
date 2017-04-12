package com.bbn.bue.common;

import com.bbn.bue.common.strings.offsets.CharOffset;
import com.bbn.bue.common.strings.offsets.OffsetRange;

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
}
