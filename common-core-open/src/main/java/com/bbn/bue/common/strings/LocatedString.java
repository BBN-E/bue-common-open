package com.bbn.bue.common.strings;

import com.bbn.bue.common.strings.offsets.ASRTime;
import com.bbn.bue.common.strings.offsets.ByteOffset;
import com.bbn.bue.common.strings.offsets.CharOffset;
import com.bbn.bue.common.strings.offsets.EDTOffset;
import com.bbn.bue.common.strings.offsets.OffsetGroup;
import com.bbn.bue.common.strings.offsets.OffsetGroupRange;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * * Class for storing and manipulating strings that have been read in from a file, without losing
 * the relationship between each character and its origin in the file from which it was read.  In
 * particular, for each character in the located string, we record a start offset and an end offset
 * of each offset type (ByteOffset, CharOffset, EDTOffset, and ASRTime).  Start offsets and end
 * offsets are zero- indexed, and both are inclusive.  E.g., if a character in the string came from
 * a single byte at position 12, then that character's start ByteOffset and end ByteOffset will both
 * be 12.  For a character that was encoded using three bytes at positions 14, 15, and 16, the start
 * ByteOffset will be 14, and the end ByteOffset will be 16.
 *
 * In unmodified LocatedStrings, the start CharOffset for each character will be equal to its end
 * CharOffset.  However, modifications that replace substrings can result in individual characters
 * whose start and end offsets are not equal, since the offsets of the replacement characters are
 * set based on the entire range of characters in the replaced substring.
 *
 * The four offset types that are currently stored for each character are:
 *
 * - CharOffset.  More accurately, this is a unicode code point offset.
 *
 * - ByteOffset.  Currently, we assume that the source string was UTF-8, and calculate byte offsets
 * by checking how many bytes it would take to encode each character.  In the future, if we did our
 * own unicode encoding, we could directly read byte offsets for other encodings.
 *
 * - EDTOffset.  EDT offsets are similra to character offsets, except that (i) any substrings
 * starting with "<" and extending to the matching ">" are skipped when counting offsets; and (ii)
 * the character "\r" is skipped when counting offsets. Note that condition (i) is *not* always
 * identical to skipping XML/SGML tags and comments.
 *
 * - ASRTime.  The start and end time of the speech signal that corresponds to a character.  ASRTime
 * must be set after a LocatedString is constructed, using setAsrStartTime() and setAsrEndTime().
 *
 * @author originally by David A. Herman, refactored by Edward Loper; translated to Java by Ryan
 *         Gabbard
 * @author rgabbard
 */
public final class LocatedString {

  public String text() {
    return content;
  }

  public int length() {
    return content.length();
  }

  /**
   * ****************************************************************** Offset accessors
   * *******************************************************************
   */
  public EDTOffset startEDTOffset() {
    return bounds.startInclusive().edtOffset();
  }

  public EDTOffset endEDTOffset() {
    return bounds.endInclusive().edtOffset();
  }

  public CharOffset startCharOffset() {
    return bounds.startInclusive().charOffset();
  }

  public CharOffset endCharOffset() {
    return bounds.endInclusive().charOffset();
  }

  public OffsetGroupRange bounds() {
    return bounds;
  }

  public List<OffsetEntry> offsetEntries() {
    return offsets;
  }

  public static LocatedString forString(final String text) {
    final OffsetGroup initialOffsets = OffsetGroup.from(new ByteOffset(0), new CharOffset(0),
        EDTOffset.asEDTOffset(0));
    return forString(text, initialOffsets);
  }

  public static LocatedString forString(final String text, final OffsetGroupRange bounds,
      final List<OffsetEntry> spanOffsets) {
    return new LocatedString(text, spanOffsets, bounds);
  }

  public static LocatedString forString(final String text, final OffsetGroup initialOffsets) {
    return forString(text, initialOffsets, false);
  }

  public static LocatedString forString(final String text, final OffsetGroup initialOffsets,
      final boolean EDTOffsetsAreCharOffsets) {
    final List<OffsetEntry> offsets =
        calculateOffsets(text, initialOffsets, EDTOffsetsAreCharOffsets);
    final OffsetGroupRange bounds = boundsFromOffsets(offsets);
    return new LocatedString(text, offsets, bounds);
  }

  /**
   * Return a LocatedString substring of this string.
   *
   * NOTE: Because it recomputes the various offsets of every character in the
   * substring, this method is *significantly* more expensive than just
   * fetching the String content of the substring.  If you just need the String
   * content, you should use rawSubstring() instead.
   */
  public LocatedString substring(final OffsetGroup start, final OffsetGroup end) {
    return substring(start.charOffset(), end.charOffset());
  }

  /**
   * Return a LocatedString substring of this string.
   *
   * NOTE: Because it recomputes the various offsets of every character in the
   * substring, this method is *significantly* more expensive than just
   * fetching the String content of the substring.  If you just need the String
   * content, you should use rawSubstring() instead.
   */
  public LocatedString substring(final CharOffset start, final CharOffset end) {
    final int startOffset = start.value() - bounds.startInclusive().charOffset().value();
    final int endOffset = end.value() - bounds.startInclusive().charOffset().value() + 1;

    return substring(startOffset, endOffset);
  }

	/*public LocatedString substringConvertP(OffsetGroup start, OffsetGroup end) {
                final int startOffset = start.charOffset().value() - bounds.start.charOffset().value();
		final int endOffset = end.charOffset().value() - bounds.start.charOffset().value() + 1;

		return substringConvertP(startOffset, endOffset);
	}

	public LocatedString substringConvertP(int startIndexInclusive, int endIndexExclusive) {
		final String text = content.substring(startIndexInclusive, endIndexExclusive);
		final String text2 = text.replace("</P>", "");
		final List<OffsetEntry> offsets = offsetsOfSubstring(startIndexInclusive, endIndexExclusive);
		final OffsetRange bounds = boundsFromOffsets(offsets);
		System.out.println(new LocatedString(text2, offsets, bounds));
		return new LocatedString(text2, offsets, bounds);
	}*/

  /**
   * Return a LocatedString substring of this string.
   *
   * NOTE: Because it recomputes the various offsets of every character in the
   * substring, this method is *significantly* more expensive than just
   * fetching the String content of the substring.  If you just need the String
   * content, you should use rawSubstring() instead.
   */
  public LocatedString substring(final int startIndexInclusive, final int endIndexExclusive) {
    final String text = content.substring(startIndexInclusive, endIndexExclusive);
    final List<OffsetEntry> offsets = offsetsOfSubstring(startIndexInclusive, endIndexExclusive);
    final OffsetGroupRange bounds = boundsFromOffsets(offsets);
    return new LocatedString(text, offsets, bounds);
  }

  /**
   * Return a String substring of this string.
   *
   * @param start
   * @param end
   * @return
   */
  public String rawSubstring(final OffsetGroup start, final OffsetGroup end) {
    return rawSubstring(start.charOffset(), end.charOffset());
  }

  /**
   * Return a String substring of this string.
   *
   * @param start
   * @param end
   * @return
   */
  public String rawSubstring(final CharOffset start, final CharOffset end) {
    final int startOffset = start.value() - bounds.startInclusive().charOffset().value();
    final int endOffset = end.value() - bounds.startInclusive().charOffset().value() + 1;

    return rawSubstring(startOffset, endOffset);
  }

  /**
   * Return a String substring of this string.
   *
   * @param startIndexInclusive
   * @param endIndexExclusive
   * @return
   */
  public String rawSubstring(final int startIndexInclusive, final int endIndexExclusive) {
    return content.substring(startIndexInclusive, endIndexExclusive);
  }

  /**
   * Returns the earliest offset group within this {@code LocatedString} whose character offset
   * matches the one supplied. If not such offset group exists, throws a {@link
   * NoSuchElementException}.
   */
  public OffsetGroup offsetGroupForCharOffset(final CharOffset offset) {
    // if this ever slows us down significantly, we can binary search
    for (final OffsetEntry entry : offsets) {
      if (entry.startOffset.charOffset().value() <= offset.value()
          && entry.endOffset.charOffset().value() > offset.value()) {
        // we assume EDT offsets are continuous witihn entries
        final int offsetWithinEntry = offset.value() - entry.startOffset.charOffset().value();

        return OffsetGroup
            .from(offset, new EDTOffset(entry.startOffset.edtOffset().value() + offsetWithinEntry));
      }
    }
    throw new NoSuchElementException();
  }

  /**
   * ***************************************************************************** Private
   * implementation
   */

  private final String content;
  private final OffsetGroupRange bounds;
  private final List<OffsetEntry> offsets;

  private LocatedString(final String content, final List<OffsetEntry> offsets,
      final OffsetGroupRange bounds) {
    this.content = content;
    this.bounds = bounds;
    // since this is a private constructor, no need to defensively copy to preserve immutability
    this.offsets = offsets;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(content, bounds, offsets);
  }

  @Override
  /**
   * Equality for this is quite strict - it must be exactly the same string and offsets
   * with exactly the same interior material omitted, if any.
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final LocatedString other = (LocatedString) obj;
    return Objects.equal(this.bounds, other.bounds) && Objects.equal(this.content, other.content)
        && Objects.equal(this.offsets, other.offsets);
  }

  public static class OffsetEntry {

    private final int startPos;
    private final int endPos;
    private final OffsetGroup startOffset;
    private final OffsetGroup endOffset;
    private final boolean isEDTSkipRegion;

    public OffsetEntry(final int startPos, final int endPos, final OffsetGroup startOffset,
        final OffsetGroup endOffset, final boolean isEDTSkipRegion) {
      this.startPos = startPos;
      this.endPos = endPos;
      this.startOffset = startOffset;
      this.endOffset = endOffset;
      this.isEDTSkipRegion = isEDTSkipRegion;
    }

    public int startPos() {
      return startPos;
    }

    public int endPos() {
      return endPos;
    }

    public OffsetGroup startOffset() {
      return startOffset;
    }

    public OffsetGroup endOffset() {
      return endOffset;
    }

    public boolean isEDTSkipRegion() {
      return isEDTSkipRegion;
    }

    @Override
    public String toString() {
      return "start: " + startPos +
          "\nend: " + endPos +
          "\nstartOffset: " + startOffset +
          "\nendOffset: " + endOffset +
          "\nEDTSkip: " + isEDTSkipRegion;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(startPos, endPos, startOffset, endOffset, isEDTSkipRegion);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      final OffsetEntry other = (OffsetEntry) obj;
      return Objects.equal(this.startPos, other.startPos) && Objects
          .equal(this.endPos, other.endPos) && Objects.equal(this.startOffset, other.startOffset)
          && Objects.equal(this.endOffset, other.endOffset) && Objects
          .equal(this.isEDTSkipRegion, other.isEDTSkipRegion);
    }
  }

  private static List<OffsetEntry> calculateOffsets(final String text,
      final OffsetGroup initialOffsets, final boolean EDTOffsetsAreCharOffsets) {
    checkNotNull(text);
    checkNotNull(initialOffsets);
    checkArgument(initialOffsets.byteOffset().isPresent());

    final ImmutableList.Builder<OffsetEntry> offsets = ImmutableList.builder();

    final Optional<ASRTime> weDontKnowASRTime = Optional.absent();
    int inTag = 0;
    int byteOffset = initialOffsets.byteOffset().get().value();
    int charOffset = initialOffsets.charOffset().value();
    int edtOffset = initialOffsets.edtOffset().value();
    int pos = 0;
    int startPos = 0;
    boolean justLeftXMLTag = false;
    char prevChar = 0;
    OffsetGroup start = initialOffsets;

    // TODO: figure out how this works with UTF-16 unicode encoding...
    for (; pos < text.length(); ++pos) {
      final char c = text.charAt(pos);
      if (!EDTOffsetsAreCharOffsets && pos > 0 &&
          (inTag == 0 && (c == '<' || prevChar == '\r') || justLeftXMLTag)
          && !(justLeftXMLTag && c == '<')) {
        final int prevEDTOffset =
            (edtOffset == 0 || prevChar == '\r') ? edtOffset : (edtOffset - 1);
        offsets.add(
            new OffsetEntry(startPos, pos, start, OffsetGroup.from(new ByteOffset(byteOffset - 1),
                new CharOffset(charOffset - 1), new EDTOffset(prevEDTOffset)), justLeftXMLTag));
        startPos = pos;
        final int startEDTOffset = (c == '<') ? edtOffset - 1 : edtOffset;
        start = OffsetGroup.from(new ByteOffset(byteOffset), new CharOffset(charOffset),
            new EDTOffset(startEDTOffset));
      }

      ++charOffset;
      byteOffset += UTF8BytesInChar(c);
      if (EDTOffsetsAreCharOffsets || (!(inTag != 0 || c == '<' || c == '\r'))) {
        ++edtOffset;
      }
      if (!EDTOffsetsAreCharOffsets) {
        justLeftXMLTag = false;
        if (c == '<') {
          ++inTag;
        } else if (inTag > 0 && c == '>') {
          --inTag;
          if (inTag == 0) {
            justLeftXMLTag = true;
          }
        }
      }
      prevChar = c;
    }
    if (pos > startPos) {
      final int prevEDTOffset = Math.max(start.edtOffset().value(), edtOffset - 1);
      offsets.add(new OffsetEntry(startPos, pos, start,
          OffsetGroup.from(new ByteOffset(byteOffset - 1), new CharOffset(charOffset - 1),
              new EDTOffset(prevEDTOffset)), inTag > 0 || justLeftXMLTag));
    }
    return offsets.build();
  }

  private static OffsetGroupRange boundsFromOffsets(final List<OffsetEntry> offsets) {
    checkArgument(!offsets.isEmpty());
    return OffsetGroupRange
        .from(offsets.get(0).startOffset, offsets.get(offsets.size() - 1).endOffset);
  }

  private static final char ONE_BYTE = 0x007f;
  private static final char TWO_BYTE = 0x07ff;
  private static final char THREE_BYTE = 0xffff;

  private static final int UTF8BytesInChar(final char c) {
    if (c <= ONE_BYTE) {
      return 1;
    } else if (c <= TWO_BYTE) {
      return 2;
    } else if (c <= THREE_BYTE) {
      return 3;
    } else {
      return 4;
    }
  }

  /**
   * Returns offsets corresponding to substring, in order.
   */
  private List<OffsetEntry> offsetsOfSubstring(final int startIndexInclusive,
      final int endIndexExclusive) {
    checkArgument(startIndexInclusive < endIndexExclusive,
        String.format("Start Index %d not less than end index %s", startIndexInclusive,
            endIndexExclusive));

    final ImmutableList.Builder<OffsetEntry> ret = ImmutableList.builder();

    for (int entryNum = entryBefore(startIndexInclusive); entryNum < offsets.size(); ++entryNum) {
      final OffsetEntry entry = offsets.get(entryNum);
      checkArgument(entry.startPos <= endIndexExclusive);

      int newStartPos = entry.startPos;
      int newEndPos = entry.endPos;
      OffsetGroup newStartOffset = entry.startOffset;
      OffsetGroup newEndOffset = entry.endOffset;

      final int charLength =
          entry.endOffset.charOffset().value() - entry.startOffset.charOffset().value();
      final int edtLength =
          entry.endOffset.edtOffset().value() - entry.startOffset.edtOffset().value();

			/* DK: Recalculation of EDT offset assumes that within this OffsetEntry, there is no longer any difference
			 * between edt chars and actual chars.  The checkArgument call makes this assumption explicit, allowing the
			 * calculation of a new, correct edt offset.
			 *
			 * A special case is when the EDT offset length of this entry is 0, for example, if this entry is a URL in angle brackets.
			 * In this case, the edt offsets remain the same.
			 */

      if (entry.startPos < startIndexInclusive) {
        newStartPos = startIndexInclusive;

        checkArgument(charLength == edtLength || edtLength == 0);
        int newEDTOffsetValue = entry.startOffset.edtOffset().value();
        if (edtLength != 0) {
          newEDTOffsetValue += (startIndexInclusive - entry.startPos);
        }
        newStartOffset =
            OffsetGroup.from(new CharOffset(startIndexInclusive), new EDTOffset(newEDTOffsetValue));
      }
      if (entry.endPos > endIndexExclusive) {
        newEndPos = endIndexExclusive;

        checkArgument(charLength == edtLength || edtLength == 0);
        int newEDTOffsetValue = entry.endOffset.edtOffset().value();
        if (edtLength != 0) {
          newEDTOffsetValue -= (entry.endPos - endIndexExclusive);
        }
        newEndOffset = OffsetGroup
            .from(new CharOffset(endIndexExclusive - 1), new EDTOffset(newEDTOffsetValue));
      }
      newStartPos -= startIndexInclusive;
      newEndPos -= startIndexInclusive;
      ret.add(new OffsetEntry(newStartPos, newEndPos, newStartOffset, newEndOffset,
          entry.isEDTSkipRegion));

      if (entry.endPos >= (endIndexExclusive - startIndexInclusive)) {
        break;
      }
    }

    return ret.build();
  }

  private int entryBefore(final int pos) {
    int i = 1;
    while (i < offsets.size() && offsets.get(i).startPos <= pos) {
      ++i;
    }
    return i - 1;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("bounds", bounds).add("content", content).toString();
  }


}
