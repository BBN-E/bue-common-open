package com.bbn.bue.common.strings;

import com.bbn.bue.common.StringUtils;
import com.bbn.bue.common.TextGroupImmutable;
import com.bbn.bue.common.UnicodeFriendlyString;
import com.bbn.bue.common.UnicodeUnsafe;
import com.bbn.bue.common.strings.offsets.ByteOffset;
import com.bbn.bue.common.strings.offsets.CharOffset;
import com.bbn.bue.common.strings.offsets.EDTOffset;
import com.bbn.bue.common.strings.offsets.OffsetGroup;
import com.bbn.bue.common.strings.offsets.OffsetGroupRange;
import com.bbn.bue.common.strings.offsets.OffsetRange;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.immutables.value.Value;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.getLast;

/**
 * Stores and manipulates strings in a way that tracks the relationship between each Unicode
 * character in some string (the {@link #content()}) and each corresponding Unicode character in
 * some original {@link #referenceString()}, which may or may not be accessible (this is typically
 * the contents of the file a string was originally read from).
 *
 * <h2>{@code LocatedString} as character mapping</h2>
 *
 * <p>Conceptually, for each character in the {@link #content()} of {@code LocatedString},
 * we record a start offset and an end offset of each of four offset types: ({@link ByteOffset},
 * {@link CharOffset}, {@link EDTOffset}, and {@link com.bbn.bue.common.strings.offsets.ASRTime}).
 * These encode the corresponding offsets <i>in the reference string</i>.  Start offsets and
 * end offsets are zero-indexed, and both are inclusive.  For example, if a character in the string
 * came from a single byte at position 12, then that character's start {@code ByteOffset} and end
 * {@code ByteOffset} will both be 12.  For a character that was encoded using three bytes at
 * positions 14, 15, and 16, the start {@code ByteOffset} will be 14, and the end {@code ByteOffset}
 * will be 16.</p>
 *
 *  <p>In {@code LocatedString}s which have not undergone any transformations, the start
 * and end {@code CharOffset}s for each character will be equal (this may not hold for other offsets,
 * in particular {@link com.bbn.bue.common.strings.offsets.ASRTime}).  However, modifications that
 * replace substrings can result in individual characters whose start and end offsets are not equal,
 * since the offsets of the replacement characters are set based on the entire range of characters
 * in the replaced substring. Similarly, when material is inserted, the inserted material may
 * be arbitrarily mapped to the text before or after it.</p>
 *
 * <p>While conceptually a mapping is stored for each character, in practice we use a more
 * efficient internal representation, described below. This internal representation is exposed
 * through {@link #characterRegions()} solely for the use of I/O code. Please do not use it.</p>
 *
 * <h2>Offset types</h2>
 *
 * <p>The four offset types that are currently stored for each character are:
 *
 * <ul>
 *
 * <li><b>CharOffset</b>.  More accurately, this is a Unicode code point offset.  Note this means
 * that non-BMP Unicode characters will be counted as one despite being two {@code char}s in Java.</li>
 *
 * <li><b>ByteOffset</b>. Optional.</li>
 *
 * <li><b>EDTOffset</b>.  Historically-speaking, EDT offsets are similar to character offsets,
 * except that (i) any substrings starting with "<" and extending to the matching ">" are skipped
 * when counting offsets; and (ii) the character "\r" is skipped when counting offsets.
 * Note that condition (i) is *not* always identical to skipping XML/SGML tags and comments.
 *
 * Nothing in JSerif enforces this interpretation of EDT offsets, so you could use them for
 * other purposes. However, the use of EDT offsets in other ways may cause problems interfacing
 * with CSerif. Note however, that CSerif itself has modes which produce different EDT offsets.
 * It is recommended that you not use EDT offsets.</li>
 *
 * <li><b>ASRTime</b>.  Optional. The start and end time of the speech signal that corresponds to a character.
 * </li>
 *
 * </ul></p>
 *
 * <p>Byte and ASR offsets are typically lost by substring and other operations on
 * {@link LocatedString} since there is usually insufficient information to determine what
 * they should be.</p>
 *
 * <h2>Substrings</h2>
 *
 * There are many possible ways to get substrings of a {@link LocatedString}.  There are three
 * dimensions to consider:
 * <ul>
 *   <li>do you want a substring of {@link #content()}</li> or of the {@link #referenceString()}?</li>
 *   <li>do you want the substring to retain offset mapping information or to be
 *   a raw {@link UnicodeFriendlyString}? If you don't need it, dropping the offset mapping
 *   is much faster.  Note any substrings of the reference string will always lack offset
 *   mapping, since it doesn't make sense.</li>
 *   <li>do you want to specify the bounds of your substring by code point offsets in
 *   the content string or in the reference string?</li>
 * </ul>
 *
 * Here are how all the combinations translate to methods:
 * <ul>
 *
 *   <li><b>substring of content, with offset mapping, by content bounds:</b>
 *   {@link #contentLocatedSubstringByContentOffsets(OffsetRange)}</li>
 *
 *   <li><b>substring of content, with offset mapping, by reference bounds:</b>
 *   {@link #contentLocatedSubstringByReferenceOffsets(OffsetRange)}. Currently unimplemented.</li>
 *
 *   <li><b>substring of content, without offset mapping, by content bounds:</b>
 *   {@link #content()}'s {@link UnicodeFriendlyString#substringByCodePoints(CharOffset, CharOffset)}</li>
 *
 *   <li><b>substring of content, without offset mapping, by reference bounds:</b>
 *   {@link #contentSubstringByReferenceOffsets(OffsetRange)}. Currently unimplemented.</li>
 *
 *   <li><b>substring of reference, without offset mapping, by content bounds:</b>
 *   {@link #referenceSubstringByContentOffsets(OffsetRange)}</li>
 *
 *   <li><b>substring of reference, without offset mapping, by reference bounds:</b>
 *       {@link #referenceString()}'s {@link UnicodeFriendlyString#substringByCodePoints(CharOffset, CharOffset)}.
 * </li>
 *
 * </ul>
 *
 * <h2>Other notes</h2>
 * <p>Equality for {@code LocatedString} is quite strict: not only is the content required to be
 * identical, but so is the mapping between the content and the original text offsets.</p>
 *
 * <h2>Internal Representation</h2>
 *
 * <b>WARNING:</b> This section is for the benefit of future developers working on
 * {@link LocatedString} internals.
 * The implementation of {@link LocatedString} is exposed entirely for the sake of I/O and
 * is not guaranteed to be stable.  Do not use {@link CharacterRegion} directly in your code.
 *
 * A {@code LocatedString} is represented by a sequence of  {@link CharacterRegion}s representing
 * spans of content characters within which various reference string offsets can be
 * straightforwardly calculated. To spell this out in detail, within each region, the following
 * must hold:
 *
 * <ul>
 *   <li>Every region must be made uniformly of code points either within the BMP or outside of it.
 *   BMP and non-BMP code points cannot be mixed in a region.  This must hold true of both the
 *   content and reference string text.</li>
 *   <li>With respect to content string character offsets, exactly one of the following must hold:
 *   <ul>
 *     <li>Content string character offsets must correspond one-to-one with reference string
 *        character offsets. (most common)</li>
 *     <li>The reference string start and end character offsets must be equal (text was
 *     transformed by insertion)</li>
 *     <li>The content string start and end character offsets must be equal (text was transformed
 *     by deletion).</li>
 *   </ul></li>
 *   <li>Every region must either have its start and end reference text EDT offsets equal to one
 *   another or the reference text EDT offsets must be in one-to-one correspondence with the
 *   reference text character offsets. That is, every either region represents an area where
 *   EDT offset counting rules don't apply or an area which is skipped for EDT offset counting.</li>
 * </ul>
 *
 * <p>The regions are required to be disjoint on the content text side and to cover it
 * completely.  Adjacent regions *must* differ in their mapping properties in some way;
 * however this "canonicalization" will occur automatically when the object is built, so
 * users do not need to concern themselves with it. However, we exploit this property in e.g.
 * string containment checks.</p>
 *
 * @author originally by David A. Herman, refactored by Edward Loper; translated to Java
 * and later restructured significantly by Ryan Gabbard
 */
@JsonSerialize(as = ImmutableLocatedString.class)
@JsonDeserialize(as = ImmutableLocatedString.class)
@TextGroupImmutable
@Value.Immutable(prehash = true)
@Value.Enclosing
public abstract class LocatedString {

  // Data fields

  /**
   * The string represented by this LocatedString. This may or may not match the original text it
   * came from (see class Javadoc).
   */
  public abstract UnicodeFriendlyString content();

  /**
   * This is the original string this located string is derived from before any transformations.
   * Note that when e.g. taking substrings of a located string, the pointer to the full original
   * reference string is maintained.
   */
  public abstract Optional<UnicodeFriendlyString> referenceString();

  /**
   * {@link CharacterRegion}s track the relationship between offsets and indices in the
   * LocatedString. What this relationship is, exactly, can vary between different parts of a
   * string. Examples: (a) inside an XML tag, LocatedString indices and character offsets will
   * be incremented, but EDT offsets will not. (b) When text is inserted, many LocatedString
   * offsets will correspond to the same character offset.
   *
   * These regions must be in order, gap-less with respect to their content string offsets, and
   * non-overlapping with respect to their content string positions.
   */
  public abstract ImmutableList<CharacterRegion> characterRegions();

  // Derived fields

  /**
   * The offsets in the reference string that {@link #content()} corresponds to.  This is roughly
   * the "convex hull" of these reference string offsets. That is, there will be content string
   * characters corresponding to the start and end reference string offsets of these bounds, but
   * there may be intermediate reference string offsets with no corresponding content string
   * position.
   */
  @Value.Derived
  public OffsetGroupRange referenceBounds() {
    // we enforce in check that regions is not empty and we do not access this method in check()
    return OffsetGroupRange.from(
        characterRegions().iterator().next().referenceStartOffsetInclusive(),
        getLast(characterRegions()).referenceEndOffsetInclusive());
  }

  @Override
  public final String toString() {
    return content() + " [" + referenceBounds() + "]";
  }

  // Means of creation

  /**
   * Makes the provided string into a {@link LocatedString} with a one-to-one mapping
   * between content characters and reference string characters and with EDT offsets equal
   * to character offsets.  This is typically how a string should originate before any
   * further transformations are applied.
   */
  public static LocatedString fromReferenceString(final String text) {
    return new OffsetCalculator.Builder().build().calculateOffsets(text);
  }

  /**
   * Makes the provided string into a {@link LocatedString} with a one-to-one mapping
   * between content characters and reference string characters and with EDT offsets equal
   * to character offsets.  This is typically how a string should originate before any
   * further transformations are applied.
   */
  public static LocatedString fromReferenceString(final UnicodeFriendlyString text) {
    return new OffsetCalculator.Builder().build().calculateOffsets(text);
  }

  /**
   * Users should not construct {@code LocatedString} directly from a builder except in
   * test and I/O code.  Prefer {@link #fromReferenceString(UnicodeFriendlyString)}.
   */
  public static class Builder extends ImmutableLocatedString.Builder {

  }

  // offset mapping

  /**
   * Get the earliest reference string offsets corresponding to the given content string offset.
   */
  public OffsetGroup startReferenceOffsetsForContentOffset(CharOffset contentOffset) {
    return characterRegions().get(regionIndexContainingContentOffset(contentOffset))
        .startOffsetGroupForPosition(contentOffset);
  }

  /**
   * Get the latest reference string offsets corresponding to the given content string offset.
   */
  public OffsetGroup endReferenceOffsetsForContentOffset(CharOffset contentOffset) {
    return characterRegions().get(regionIndexContainingContentOffset(contentOffset))
        .endOffsetGroupForPosition(contentOffset);
  }


  // substrings

  /**
   * Return a {@code LocatedString} substring of this string.  The substring and the indices are
   * both with respect to the {@code LocatedString}'s content, not its reference string.
   * Please refer to the class Javadoc for coverage of available substring options.
   *
   * <p><b>NOTE:</b> Because it recomputes the various offsets of every character in the
   * substring, this method is *significantly* more expensive than just
   * fetching the String content of the substring.  If you just need the String
   * content, you should use rawSubstring() instead.</p>
   */
  public final LocatedString contentLocatedSubstringByContentOffsets(
      final OffsetRange<CharOffset> contentOffsets) {
    final UnicodeFriendlyString substringText = content().substringByCodePoints(contentOffsets);
    final List<CharacterRegion> substringOffsets =
        offsetsOfSubstringByContentCodepointOffsets(contentOffsets);
    return new LocatedString.Builder().content(substringText).referenceString(referenceString())
        .characterRegions(substringOffsets).build();
  }

  /**
   * Return a {@code LocatedString} substring of this string containing the content offsets
   * covering the specified range of reference offsets. Please refer to the class Javadoc
   * for coverage of available substring options.
   *
   * This method is currently unimplemented.
   */
  @SuppressWarnings("unused")
  public final LocatedString contentLocatedSubstringByReferenceOffsets(
      final OffsetRange<CharOffset> contentOffsets) {
    throw new UnsupportedOperationException();
  }

  /**
   * Return a substring of this string containing the content offsets
   * covering the specified range of reference offsets. Please refer to the class Javadoc
   * for coverage of available substring options.
   *
   * This method is currently unimplemented.
   */
  @SuppressWarnings("unused")
  public final UnicodeFriendlyString contentSubstringByReferenceOffsets(
      final OffsetRange<CharOffset> contentOffsets) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets the substring of the reference string corresponding to a range of content string
   * offsets.  This will include intervening characters which may not themselves correspond to
   * any content string character.
   *
   * Will return {@link Optional#absent()} if and only if {@link #referenceString()} returns
   * absent.
   *
   * Please refer to the class Javadoc for coverage of available substring options.
   */
  public final Optional<UnicodeFriendlyString> referenceSubstringByContentOffsets(
      final OffsetRange<CharOffset> contentOffsets) {
    if (referenceString().isPresent()) {
      return Optional.of(referenceString().get().substringByCodePoints(
          OffsetGroupRange.from(
              startReferenceOffsetsForContentOffset(contentOffsets.startInclusive()),
              endReferenceOffsetsForContentOffset(contentOffsets.endInclusive()))
              .asCharOffsetRange()));
    } else {
      return Optional.absent();
    }
  }

  /**
   * Does this substring contain {@code other} <b>exactly</b>, which is to say, not only is the
   * content text of {@code other} contained in this one, but for that text, the corresponding
   * reference offset mappings are exactly the same as well.
   *
   * Note that for most applications doing containment checks on either {@link #content()}
   * or {@link #referenceBounds()} suffices and is much faster.
   * </p>
   */
  public final boolean containsExactly(LocatedString other) {
    if (!referenceCharOffsetsSequential() && other.referenceCharOffsetsSequential()) {
      throw new UnsupportedOperationException("Containment for non-monotonic LocatedStrings needs"
          + " to be implemented");
    }

    if (!referenceBounds().asCharOffsetRange().contains(other.referenceBounds().asCharOffsetRange())
        || !referenceBounds().asEdtOffsetRange()
        .contains(other.referenceBounds().asEdtOffsetRange())) {
      // quick, cheap short-circuit check
      return false;
    }

    // which of our character regions contains the start character offset of the putative substring?
    final Optional<Integer> startRegionIndex =
        firstRegionIndexContainingReferenceCharOffset(other.referenceBounds().startCharOffsetInclusive());
    // which of our character regions contains the end character offset of the putative substring?
    final Optional<Integer> endRegionIndex =
        firstRegionIndexContainingReferenceCharOffset(other.referenceBounds().endCharOffsetInclusive());

    // if we don't have any region containing them, it can't be a substring
    if (!startRegionIndex.isPresent() || !endRegionIndex.isPresent()) {
      return false;
    }

    final int numThisRegionsPossiblyOverlappingSubstring =
        endRegionIndex.get() - startRegionIndex.get() + 1;

    // we know if they are a substring this must be equal because both are in canonical form
    if (numThisRegionsPossiblyOverlappingSubstring != other.characterRegions().size()) {
      return false;
    }

    final CharOffset earliestPossibleMatchingContentOffset;
    final CharOffset latestPossibleMatchingContentOffset;

    if (numThisRegionsPossiblyOverlappingSubstring == 1) {
      // special case for when the putative substring would be contained entirely in just one
      // of our character regions
      final CharacterRegion containerOnlyMatchingRegion =
          characterRegions().get(startRegionIndex.get());
      final CharacterRegion containeeOnlyRegion = other.characterRegions().get(0);
      if (containerOnlyMatchingRegion.contains(containeeOnlyRegion)) {
        earliestPossibleMatchingContentOffset =
            containerOnlyMatchingRegion.absoluteStartingContentOffsetOfReferenceCharOffset(
                containeeOnlyRegion.referenceStartOffsetInclusive().charOffset());
        latestPossibleMatchingContentOffset =
            containerOnlyMatchingRegion.absoluteEndingContentOffsetOfReferenceCharOffset(
                containeeOnlyRegion.referenceEndOffsetInclusive().charOffset());
      } else {
        return false;
      }
    } else {
      // if the putative substring covers more than one of our character regions, then
      // its first region must be a suffix of our first region...
      final CharacterRegion containerFirstMatchingRegion =
          characterRegions().get(startRegionIndex.get());
      final CharacterRegion containeeFirstRegion = other.characterRegions().get(0);
      if (!containeeFirstRegion.isSuffixOf(containerFirstMatchingRegion)) {
        return false;
      }
      earliestPossibleMatchingContentOffset =
          containerFirstMatchingRegion.absoluteStartingContentOffsetOfReferenceCharOffset(
              containeeFirstRegion.referenceStartOffsetInclusive().charOffset());

      // and its last region must be a prefix of our last region
      final CharacterRegion containerLastMatchingRegion =
          characterRegions().get(endRegionIndex.get());
      final CharacterRegion containeeLastRegion = Iterables.getLast(other.characterRegions());
      if (!containeeLastRegion.isPrefixOf(containerLastMatchingRegion)) {
        return false;
      }
      latestPossibleMatchingContentOffset =
          containerLastMatchingRegion.absoluteEndingContentOffsetOfReferenceCharOffset(
              containeeLastRegion.referenceEndOffsetInclusive().charOffset());

      // and all intermediate regions must match exactly (because of canonical form)
      for (int i = 1; i < numThisRegionsPossiblyOverlappingSubstring - 1; ++i) {
        final CharacterRegion thisRegion = characterRegions().get(startRegionIndex.get() + i);
        final CharacterRegion otherRegion = other.characterRegions().get(i);
        if (!thisRegion.equivalentUpToShiftedContentOffsets(otherRegion)) {
          return false;
        }
      }
    }

    return content().substringByCodePoints(OffsetRange.fromInclusiveEndpoints(
        earliestPossibleMatchingContentOffset, latestPossibleMatchingContentOffset))
        .contains(other.content());
  }

  // private implementation

  /**
   * Describes a region of the content string whose relationship to the reference string
   * can be computed by some consistent rule for all character offsets in the region.
   *
   * This is an aspect of the internal representation of {@link LocatedString} which is exposed
   * only for I/O by SerifXMLLoader. Users should never access {@link CharacterRegion}s directly.
   */
  @TextGroupImmutable
  @Value.Immutable(prehash = true)
  public static abstract class CharacterRegion {

    /**
     * Are any of the content string characters for this region non-BMP?
     */
    public abstract boolean contentNonBmp();

    /**
     * The beginning offset of this region in the content string.
     */
    public abstract CharOffset contentStartPosInclusive();

    /**
     * The offset after the end offset of this region in the content string.
     */
    public abstract CharOffset contentEndPosExclusive();

    /**
     * The reference offsets corresponding to the first content character.
     */
    public abstract OffsetGroup referenceStartOffsetInclusive();

    /**
     * The reference offsets corresponding to the last content character.
     */
    public abstract OffsetGroup referenceEndOffsetInclusive();

    @Override
    public String toString() {
      return "OffsetEntry{pos: [" + contentStartPosInclusive() + ", " + contentEndPosExclusive()
          + "]; "
          + OffsetGroupRange.from(referenceStartOffsetInclusive(), referenceEndOffsetInclusive())
          + "}";
    }

    @Value.Check
    protected void check() {
      // every region must cover some portion of the content string
      checkArgument(contentCodePointLength() > 0);
      // ensure that correspondence between content CharOffsets and reference CharOffsets is simple
      checkArgument(contentAndReferenceCodePointsMatch() || isInsertion() || isDeletion());
      // ensure that correspondence between content CharOffsets and reference EdtOffsets is simple
      checkArgument(contentAndReferenceEdtOffsetsMatch() || isEdtSkipRegion() || isDeletion());
      checkArgument(referenceEndOffsetInclusive().charOffset().asInt()
          >= referenceStartOffsetInclusive().charOffset().asInt());
      // if reference character offsets are not being updated, reference EDT offsets can't either
      checkArgument(referenceCodePointLength() > 0 || referenceEdtLength() == 0);
    }

    /**
     * The size of this region on the reference side, in code points.
     */
    private int referenceCodePointLength() {
      // +1 because offsets are inclusive
      return referenceEndOffsetInclusive().charOffset().asInt() - referenceStartOffsetInclusive()
          .charOffset().asInt() + 1;
    }

    /**
     * The size of this region on the content size, in code points.
     */
    private int contentCodePointLength() {
      // no +1 because end offset is exclusive
      return contentEndPosExclusive().asInt() - contentStartPosInclusive().asInt();
    }

    /**
     * Whether this region represents content inserted with no corresponding reference text.
     */
    private boolean isInsertion() {
      return contentCodePointLength() > 1 && referenceCodePointLength() == 1;
    }

    /**
     * Whether this region represents content deleted from the reference text.
     */
    private boolean isDeletion() {
      return contentCodePointLength() == 1 && referenceCodePointLength() > 1;
    }

    /**
     * Whether there is a one-to-one mapping between content and reference offsets over this
     * region.
     */
    private boolean contentAndReferenceCodePointsMatch() {
      return referenceCodePointLength() == contentCodePointLength();
    }

    /**
     * Whether there is a one-to-one mapping between content and EDT offsets over this
     * region.
     */
    private boolean contentAndReferenceEdtOffsetsMatch() {
      return referenceCodePointLength() == referenceEdtLength();
    }

    /**
     * The size of this region in terms of EDT offsets.
     */
    private int referenceEdtLength() {
      // +1 because offsets are inclusive
      return referenceEndOffsetInclusive().edtOffset().asInt() - referenceStartOffsetInclusive()
          .edtOffset().asInt() + 1;
    }

    /**
     * Whether this is a region where EDT offsets are not incremented.
     */
    private boolean isEdtSkipRegion() {
      return referenceCodePointLength() > 0
          && referenceStartOffsetInclusive().edtOffset()
          .equals(referenceEndOffsetInclusive().edtOffset());
    }

    /**
     * The beginning of the reference offsets mapped to a content string offset.
     */
    private OffsetGroup startOffsetGroupForPosition(final CharOffset position) {
      final int offset = position.asInt() - contentStartPosInclusive().asInt();

      if (offset > 0) {
        return new OffsetGroup.Builder()
            .charOffset(startReferenceCharOffsetForRelativePosition(offset))
            .edtOffset(startReferenceEdtOffsetForRelativePosition(offset))
            .build();
      } else {
        return referenceStartOffsetInclusive();
      }
    }

    /**
     * The end of the reference offsets mapped to a content string offset.
     */
    private OffsetGroup endOffsetGroupForPosition(final CharOffset position) {
      final int offset = position.asInt() - contentStartPosInclusive().asInt();

      if (offset > 0) {
        return new OffsetGroup.Builder()
            .charOffset(endReferenceCharOffsetForRelativePosition(offset))
            .edtOffset(endReferenceEdtOffsetForRelativePosition(offset))
            .build();
      } else {
        return referenceStartOffsetInclusive();
      }
    }

    /**
     * The first character offset mapped to a content string offset, where the
     * offset is specified relative to the beginning of this region.
     */
    private CharOffset startReferenceCharOffsetForRelativePosition(int relativePosition) {
      if (isInsertion() || isDeletion()) {
        return referenceStartOffsetInclusive().charOffset();
      } else {
        return CharOffset.asCharOffset(
            referenceStartOffsetInclusive().charOffset().asInt() + relativePosition);
      }
    }

    /**
     * The last character offset mapped to a content string offset, where the
     * offset is specified relative to the beginning of this region.
     */
    private CharOffset endReferenceCharOffsetForRelativePosition(int relativePosition) {
      if (isInsertion() || isDeletion()) {
        return referenceEndOffsetInclusive().charOffset();
      } else {
        return CharOffset.asCharOffset(
            referenceStartOffsetInclusive().charOffset().asInt() + relativePosition);
      }
    }

    /**
     * The first EDT offset mapped to a content string offset, where the
     * offset is specified relative to the beginning of this region.
     */
    private EDTOffset startReferenceEdtOffsetForRelativePosition(int relativePosition) {
      // within any region, either the EDT offsets are constant or increase in lockstep with
      // the content offsets
      if (isInsertion() || isDeletion() || isEdtSkipRegion()) {
        return referenceStartOffsetInclusive().edtOffset();
      } else {
        return EDTOffset.asEDTOffset(
            referenceStartOffsetInclusive().edtOffset().asInt() + relativePosition);
      }
    }

    /**
     * The last EDT offset mapped to a content string offset, where the
     * offset is specified relative to the beginning of this region.
     */
    private EDTOffset endReferenceEdtOffsetForRelativePosition(int relativePosition) {
      // within any region, either the EDT offsets are constant or increase in lockstep with
      // the content offsets
      if (isInsertion() || isDeletion() || isEdtSkipRegion()) {
        return referenceEndOffsetInclusive().edtOffset();
      } else {
        return EDTOffset.asEDTOffset(
            referenceStartOffsetInclusive().edtOffset().asInt() + relativePosition);
      }
    }

    /**
     * Given content offsets within this region, makes a new {@link CharacterRegion} describing
     * the mapping for the span only.
     */
    private CharacterRegion fromContentOffsetStartInclusiveToEndExclusive(
        CharOffset substringContentStartInclusive, CharOffset substringContentEndExclusive) {
      checkArgument(substringContentStartInclusive.followsOrEquals(contentStartPosInclusive())
          && substringContentEndExclusive.precedesOrEquals(contentEndPosExclusive()));

      final boolean isThisRegionExactly = substringContentStartInclusive.equals(contentStartPosInclusive())
          && substringContentEndExclusive.equals(contentEndPosExclusive());
      if (isThisRegionExactly) {
        return this;
      } else {
        return new CharacterRegion.Builder()
            .contentNonBmp(contentNonBmp())
            .contentStartPosInclusive(substringContentStartInclusive)
            .contentEndPosExclusive(substringContentEndExclusive)
            .referenceStartOffsetInclusive(startOffsetGroupForPosition(substringContentStartInclusive))
            .referenceEndOffsetInclusive(
                endOffsetGroupForPosition(substringContentEndExclusive.shiftedCopy(-1)))
            .build();
      }
    }

    /**
     * Given a content offset within this region, makes a new {@link CharacterRegion} describing
     * the mapping for the span from that offset to the end of the region only.
     */
    private CharacterRegion fromContentOffsetInclusiveToEnd(CharOffset substringContentStartInclusive) {
      return fromContentOffsetStartInclusiveToEndExclusive(substringContentStartInclusive,
          contentEndPosExclusive());
    }

    /**
     * Given a content offset within this region, makes a new {@link CharacterRegion} describing
     * the mapping for the span from the beginning of the region to that offset only.
     */
    private CharacterRegion fromStartToContentOffsetExclusive(CharOffset substringContentEndExclusive) {
      return fromContentOffsetStartInclusiveToEndExclusive(contentStartPosInclusive(),
          substringContentEndExclusive);
    }

    /**
     * Given an immediately following region which follows the same offset mapping rules,
     * merges the two together into a new region. This is used when {@link LocatedString}s
     * are put into canonical form during construction in {@link #check()}.
     */
    private CharacterRegion mergeFollowingRegion(final CharacterRegion followingRegion) {
      checkArgument(
          contentEndPosExclusive().precedesOrEquals(followingRegion.contentStartPosInclusive()));
      checkArgument(mayMergeWithFollowing(followingRegion));

      return new CharacterRegion.Builder()
          .contentStartPosInclusive(contentStartPosInclusive())
          .contentEndPosExclusive(followingRegion.contentEndPosExclusive())
          .contentNonBmp(contentNonBmp()) // = followingRegion.contentNonBmp by precondition
          .referenceStartOffsetInclusive(referenceStartOffsetInclusive())
          .referenceEndOffsetInclusive(followingRegion.referenceEndOffsetInclusive())
          .build();
    }

    /**
     * Would merging this region with the following maintain offset mapping?  If so and they
     * are adjacent, they must be merged to have a {@code LocatedString} in canonical form.
     */
    private boolean mayMergeWithFollowing(final CharacterRegion other) {
      return contentEndPosExclusive().equals(other.contentStartPosInclusive())
          && referenceEndOffsetInclusive().equals(other.referenceStartOffsetInclusive())
          && isDeletion() == other.isDeletion()
          && isInsertion() == other.isInsertion()
          && isEdtSkipRegion() == other.isEdtSkipRegion()
          && contentNonBmp() == other.contentNonBmp();
    }

    /**
     * True if and only if this region's offset mapping exactly matches that of {@code other} over
     * a suffix of {@code other}'s content offsets.
     */
    private boolean isSuffixOf(final CharacterRegion other) {
      return contentNonBmp() == other.contentNonBmp()
          && contentCodePointLength() <= other.contentCodePointLength()
          && referenceEndOffsetInclusive().equals(other.referenceEndOffsetInclusive())
          && other.referenceStartOffsetInclusive()
          .precedesOrEqualsForAllOffsetTypesInBoth(referenceStartOffsetInclusive());
    }

    /**
     * True if and only if this region's offset mapping exactly matches that of {@code other} over
     * a prefix of {@code other}'s content offsets.
     */
    private boolean isPrefixOf(final CharacterRegion other) {
      return contentNonBmp() == other.contentNonBmp()
          && contentCodePointLength() <= other.contentCodePointLength()
          && referenceStartOffsetInclusive().equals(other.referenceStartOffsetInclusive())
          && referenceEndOffsetInclusive()
          .precedesOrEqualsForAllOffsetTypesInBoth(other.referenceEndOffsetInclusive());
    }

    /**
     * True if and only if this region's offset mapping exactly matches that of {@code other} over
     * some contiguous subset of {@code other}'s content offsets.
     */
    private boolean contains(final CharacterRegion other) {
      return contentNonBmp() == other.contentNonBmp()
          && other.contentCodePointLength() <= contentCodePointLength()
          && referenceStartOffsetInclusive()
          .precedesOrEqualsForAllOffsetTypesInBoth(other.referenceStartOffsetInclusive())
          && referenceEndOffsetInclusive()
          .followsOrEqualsForAllOffsetTypesInBoth(other.referenceEndOffsetInclusive());
    }


    /**
     * Makes a new {@code CharacterRegion} just like this one, but with the start and end content
     * offsets shifted by the specified amount. Useful when making substrings.
     */
    private CharacterRegion shiftContentOffsets(int shiftAmount) {
      return new CharacterRegion.Builder().from(this)
          .contentStartPosInclusive(contentStartPosInclusive().shiftedCopy(shiftAmount))
          .contentEndPosExclusive(contentEndPosExclusive().shiftedCopy(shiftAmount))
          .build();
    }

    public boolean equivalentUpToShiftedContentOffsets(final CharacterRegion otherRegion) {
      return contentNonBmp() == otherRegion.contentNonBmp()
          && referenceStartOffsetInclusive().equals(otherRegion.referenceStartOffsetInclusive())
          && referenceEndOffsetInclusive().equals(otherRegion.referenceEndOffsetInclusive())
          && contentCodePointLength() == otherRegion.contentCodePointLength();
    }

    /**
     * Gets the earliest content position in the region mapped to the given reference
     * character offset
     */
    public CharOffset absoluteStartingContentOffsetOfReferenceCharOffset(
        final CharOffset referenceCharOffset) {
      checkArgument(
          referenceStartOffsetInclusive().charOffset().precedesOrEquals(referenceCharOffset)
              && referenceEndOffsetInclusive().charOffset().followsOrEquals(referenceCharOffset));

      if (isInsertion() || isDeletion()) {
        return contentStartPosInclusive();
      } else {
        final int relativePositionWithinRegion =
            referenceCharOffset.asInt() - referenceStartOffsetInclusive().charOffset().asInt();
        return contentStartPosInclusive().shiftedCopy(relativePositionWithinRegion);
      }
    }

    /**
     * Gets the latest content position in the region mapped to the given reference
     * character offset
     */
    public CharOffset absoluteEndingContentOffsetOfReferenceCharOffset(
        final CharOffset referenceCharOffset) {
      checkArgument(
          referenceStartOffsetInclusive().charOffset().precedesOrEquals(referenceCharOffset)
              && referenceEndOffsetInclusive().charOffset().followsOrEquals(referenceCharOffset));

      if (isInsertion() || isDeletion()) {
        return contentEndPosExclusive().shiftedCopy(-1);
      } else {
        final int relativePositionWithinRegion =
            referenceCharOffset.asInt() - referenceStartOffsetInclusive().charOffset().asInt();
        return contentStartPosInclusive().shiftedCopy(relativePositionWithinRegion);
      }
    }

    public static class Builder extends ImmutableLocatedString.CharacterRegion.Builder {

    }

  }

  @UnicodeUnsafe
  @Value.Check
  protected LocatedString checkValidity() {
    checkRegionsCompletelyCoverContentString();

    // ensure that if a reference string is provided, it is compatible with the region's reference
    // character offsets
    if (referenceString().isPresent()) {
      for (final CharacterRegion region : characterRegions()) {
        final CharOffset lastCodepointOffsetInReferenceString = CharOffset.asCharOffset(
            referenceString().get().lengthInCodePoints() - 1);

        checkArgument(region.referenceStartOffsetInclusive().charOffset().asInt() >= 0
            && region.referenceEndOffsetInclusive().charOffset()
            .precedesOrEquals(lastCodepointOffsetInReferenceString));
      }
    }

    final ImmutableList<CharacterRegion> canonicalRegions = canonicalize(characterRegions());
    // == is ok here because if the character regions are already canonical, canonicalize
    // promises to return the input list itself
    if (canonicalRegions != characterRegions()) {
      return new LocatedString.Builder().from(this).characterRegions(canonicalRegions)
          .build();
    } else {
      return this;
    }
  }

  private void checkRegionsCompletelyCoverContentString() {
    checkArgument(!characterRegions().isEmpty(), "LocatedString for %s lacks regions",
        content());

    checkArgument(characterRegions().iterator().next().contentStartPosInclusive().asInt() == 0,
        "First region of a located string must have a content position of 0");
    CharacterRegion lastRegion = null;
    for (final CharacterRegion region : characterRegions()) {
      if (lastRegion != null) {
        checkArgument(lastRegion.contentEndPosExclusive().equals(region.contentStartPosInclusive()),
            "There is a gap in the content string not covered by any region: %s "
                + "immediately precedes %s", lastRegion, region);
      }
      lastRegion = region;
    }
    checkArgument(getLast(characterRegions()).contentEndPosExclusive().asInt() == content()
        .lengthInCodePoints());
  }

  /**
   * Converts character regions into a canonical form where no two adjacent character regions have
   * the same offset mapping rules (that is, there is no unnecessary multiplication of character
   * regions).  We exploit this property when computing substring containment and doing equality
   * checks.
   *
   * If the input is in canonical form already, the identical list object itself is returned.
   */
  private ImmutableList<CharacterRegion> canonicalize(ImmutableList<CharacterRegion> inputRegions) {
    boolean mergedAnything = false;
    final ImmutableList.Builder<CharacterRegion> canonicalizedRegions = ImmutableList.builder();
    CharacterRegion bufferRegion = null;

    for (final CharacterRegion curRegion : inputRegions) {
      if (bufferRegion == null) {
        bufferRegion = curRegion;
      } else {
        if (bufferRegion.mayMergeWithFollowing(curRegion)) {
          mergedAnything = true;
          bufferRegion = bufferRegion.mergeFollowingRegion(curRegion);
        } else {
          canonicalizedRegions.add(bufferRegion);
          bufferRegion = curRegion;
        }
      }
    }

    if (bufferRegion != null) {
      canonicalizedRegions.add(bufferRegion);
    }

    if (mergedAnything) {
      return canonicalizedRegions.build();
    } else {
      return inputRegions;
    }
  }


  /**
   * Nothing actually requires that {@link LocatedString}'s reference character offsets increase
   * monotonically, but it is not clear to me anything ever generates things which don't and
   * dealing with them is very difficult. So where it matters, we will check this and throw
   * an exception for unsupported operations. Whoever needs to support this in the future is
   * responsible for dealing with it.
   */
  @Value.Derived
  protected boolean referenceCharOffsetsSequential() {
    for (int i = 1; i < characterRegions().size(); ++i) {
      if (!characterRegions().get(i - 1).referenceEndOffsetInclusive().charOffset()
          .precedesOrEquals(
              characterRegions().get(i).referenceStartOffsetInclusive().charOffset())) {
        return false;
      }
    }
    return true;
  }

  private Optional<Integer> firstRegionIndexContainingReferenceCharOffset(
      final CharOffset charOffset) {
    // lazy implementation, can make faster if needed
    for (int i = 0; i < characterRegions().size(); ++i) {
      final CharacterRegion region = characterRegions().get(i);
      if (region.referenceStartOffsetInclusive().charOffset().precedesOrEquals(charOffset)
          && region.referenceEndOffsetInclusive().charOffset().followsOrEquals(charOffset)) {
        return Optional.of(i);
      }
    }
    return Optional.absent();
  }

  /**
   * Constructs {@link CharacterRegion}s for a substring.
   * See {@link #contentLocatedSubstringByContentOffsets}
   */
  private ImmutableList<CharacterRegion> offsetsOfSubstringByContentCodepointOffsets(
      final OffsetRange<CharOffset> substringContentCodePointsRange) {
    checkArgument(substringContentCodePointsRange.startInclusive().asInt() >= 0);
    checkArgument(
        substringContentCodePointsRange.endInclusive().asInt() < content().lengthInCodePoints());

    final int startEntryIdx = regionIndexContainingContentOffset(
        substringContentCodePointsRange.startInclusive());
    final int endEntryIdx = regionIndexContainingContentOffset(
        substringContentCodePointsRange.endInclusive());

    final ImmutableList.Builder<CharacterRegion> newRegions = ImmutableList.builder();

    // the content offsets of returned substring must be shifted so return substring
    // has offsets starting at zero
    final int shift = -substringContentCodePointsRange.startInclusive().asInt();
    if (startEntryIdx == endEntryIdx) {
      newRegions.add(characterRegions().get(startEntryIdx).fromContentOffsetStartInclusiveToEndExclusive(
          substringContentCodePointsRange.startInclusive(),
          substringContentCodePointsRange.endInclusive().shiftedCopy(1))
          .shiftContentOffsets(shift));
    } else {
      final CharacterRegion newStartRegion = characterRegions().get(startEntryIdx)
          .fromContentOffsetInclusiveToEnd(substringContentCodePointsRange.startInclusive())
          .shiftContentOffsets(shift);
      final CharacterRegion newEndRegion = characterRegions().get(endEntryIdx)
          .fromStartToContentOffsetExclusive(
              substringContentCodePointsRange.endInclusive().shiftedCopy(1))
          .shiftContentOffsets(shift);

      newRegions.add(newStartRegion);
      for (int i = startEntryIdx + 1; i< endEntryIdx; ++i) {
        // internal regions are unaltered and can be used in the new LocatedString
        newRegions.add(characterRegions().get(i)
            .shiftContentOffsets(shift));
      }
      newRegions.add(newEndRegion);
    }

    return newRegions.build();
  }

  private int regionIndexContainingContentOffset(CharOffset target) {
    if (target.asInt() < 0) {
      throw new IndexOutOfBoundsException(
          "Not a valid character offset for LocatedString conent: " + target);
    }
    if (target.asInt() >= content().lengthInCodePoints()) {
      throw new IndexOutOfBoundsException("Requested code point offset " + target
          + " exceeds LocatedString code point length " + content().lengthInCodePoints());
    }

    // we know by precondition checks that characterRegions cannot be empty
    // we know by the check above the the target lies in one of our regions
    // class-level preconditions guarantee the regions cover the content string completely
    // without overlap
    int startIndex = 0;
    int endIndex = characterRegions().size() - 1;

    while (startIndex <= endIndex) {
      // if integer overflow is a problem here, we have much bigger problems...
      final int probe = (startIndex + endIndex) / 2;

      final CharacterRegion probeRegion = characterRegions().get(probe);
      final boolean targetAfterProbeStart =
          probeRegion.contentStartPosInclusive().precedesOrEquals(target);
      // follows because endPos is exclusive
      final boolean targetBeforeProbeEnd = probeRegion.contentEndPosExclusive().follows(target);

      if (targetAfterProbeStart) {
        if (targetBeforeProbeEnd) {
          // probe is pointing to the containing region
          return probe;
        } else {
          startIndex = probe + 1;
        }
      } else {
        endIndex = probe - 1;
      }
    }

    throw new IllegalStateException("Binary search for regions on LocatedStrings should not be "
        + "able to fail");
  }
}

/**
 * Given a reference string, constructs the {@link LocatedString.CharacterRegion}s necessary for
 * building a {@link LocatedString}. At the moment, only handles character offsets and
 * non-BMP Unicode issues. Does not currently support calculating special EDT offsets or
 * byte offsets, although this would be possible. Currently the produced EDT offsets are undefined:
 * users should make no assumptions whatsoever about what the resulting EDT offsets are.
 */
@TextGroupImmutable
@Value.Immutable
abstract class OffsetCalculator {

  @Value.Default
  public boolean calculateEDTOffsetsByACERules() {
    return false;
  }

  @Value.Default
  public boolean computeUtf8ByteOffsets() {
    return false;
  }

  @Value.Check
  protected void check() {
    checkArgument(!calculateEDTOffsetsByACERules(), "EDT offset calculation not currently supported");
    checkArgument(!computeUtf8ByteOffsets(),
        "Computing UTF-8 byte offsets not currently supported");
  }

  public LocatedString calculateOffsets(final String s) {
    return calculateOffsets(StringUtils.unicodeFriendly(s));
  }

  public LocatedString calculateOffsets(UnicodeFriendlyString s) {
    return new OffsetCalculation(s).calculateOffsets();
  }

  public static class Builder extends ImmutableOffsetCalculator.Builder {

  }

  private final class OffsetCalculation {

    private static final int ONE_BYTE = 0x007f;
    private static final int TWO_BYTE = 0x07ff;
    private static final int THREE_BYTE = 0xffff;
    final ImmutableList.Builder<LocatedString.CharacterRegion> regions = ImmutableList.builder();
    private final UnicodeFriendlyString s;
    private int bufferStartCharOffset = 0;
    private int curCharOffset = 0;

    OffsetCalculation(final UnicodeFriendlyString s) {
      this.s = checkNotNull(s);
    }

    void clearBuffer() {
      regions.add(new LocatedString.CharacterRegion.Builder()
          .contentStartPosInclusive(CharOffset.asCharOffset(bufferStartCharOffset))
          .contentEndPosExclusive(CharOffset.asCharOffset(curCharOffset))
          .contentNonBmp(s.hasNonBmpCharacter(
              OffsetRange.charOffsetRange(bufferStartCharOffset, curCharOffset - 1)))
          .referenceStartOffsetInclusive(
              OffsetGroup.from(CharOffset.asCharOffset(bufferStartCharOffset),
                  EDTOffset.asEDTOffset(bufferStartCharOffset)))
          .referenceEndOffsetInclusive(OffsetGroup.from(
              CharOffset.asCharOffset(curCharOffset - 1), EDTOffset.asEDTOffset(curCharOffset - 1)))
          .build());
      bufferStartCharOffset = curCharOffset;
    }

    LocatedString calculateOffsets() {
      checkNotNull(s);
      checkArgument(!s.isEmpty(), "Cannot have a LocatedString of an empty string");

      // we initialize this to the codepoint count of the first character to ensure we don't
      // trigger a buffer clear due to code point size mismatch on the first iteration through
      // the loop
      int lastCodeUnitCount = Character.charCount(Character.codePointAt(s.utf16CodeUnits(), 0));

      for (int curUtf16CodeUnit = 0; curUtf16CodeUnit < s.lengthInUtf16CodeUnits(); ) {
        final int codePoint = Character.codePointAt(s.utf16CodeUnits(), curUtf16CodeUnit);

        final int codeUnitsForCharacter = Character.charCount(codePoint);
        if (lastCodeUnitCount != codeUnitsForCharacter) {
          clearBuffer();
        }
        lastCodeUnitCount = codeUnitsForCharacter;
        curUtf16CodeUnit += codeUnitsForCharacter;
        // commented-out code intentionally left in for future re-enabling of byte offset
        // computation
        /*if (computeUtf8ByteOffsets()) {
          curByteOffset += utf8BytesForCodePoint(codePoint);
        }*/
        ++curCharOffset;
      }

      clearBuffer();

      return new LocatedString.Builder().content(s).referenceString(s)
          .characterRegions(regions.build()).build();
    }

    @SuppressWarnings("unused")
    private int utf8BytesForCodePoint(final int c) {
      // see section 3 of https://tools.ietf.org/html/rfc3629
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
  }
}
