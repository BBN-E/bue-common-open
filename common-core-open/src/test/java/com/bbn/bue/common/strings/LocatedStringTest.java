package com.bbn.bue.common.strings;

import com.bbn.bue.common.UnicodeFriendlyString;
import com.bbn.bue.common.strings.offsets.OffsetGroup;
import com.bbn.bue.common.strings.offsets.OffsetGroupRange;
import com.bbn.bue.common.strings.offsets.OffsetRange;

import org.junit.Test;

import static com.bbn.bue.common.StringUtils.unicodeFriendly;
import static com.bbn.bue.common.strings.offsets.CharOffset.asCharOffset;
import static com.bbn.bue.common.strings.offsets.OffsetGroup.fromMatchingCharAndEDT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link LocatedString}.
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
public final class LocatedStringTest {

  // two non-BMP characters
  private static final String CHEESE_WEDGE = "\uD83E\uDDC0";
  private static final String FACE_WITH_TEARS_OF_JOY = "\uD83D\uDE02";

  private static final UnicodeFriendlyString TEST1_REFERENCE =
      unicodeFriendly("Hello " + CHEESE_WEDGE + ", how are you?");
  private static final UnicodeFriendlyString TEST1_CONTENT =
      unicodeFriendly("'lo " + CHEESE_WEDGE + ", "
          + FACE_WITH_TEARS_OF_JOY + " how are you?");

  private static final LocatedString TEST_STRING1 = new LocatedString.Builder()
      .referenceString(TEST1_REFERENCE)
      .content(TEST1_CONTENT)
      .addCharacterRegions(
          // Hel --> ' (a deletion/replacement)
          new LocatedString.CharacterRegion.Builder()
              .contentNonBmp(false)
              .contentStartPosInclusive(asCharOffset(0))
              .contentEndPosExclusive(asCharOffset(1))
              // in case of replacement, entire replacement is assigned to a single
              // character
              .referenceStartOffsetInclusive(fromMatchingCharAndEDT(0))
              .referenceEndOffsetInclusive(fromMatchingCharAndEDT(0))
              .build(),
          // "lo " --> "lo " (unchanged)
          new LocatedString.CharacterRegion.Builder()
              .contentNonBmp(false)
              .contentStartPosInclusive(asCharOffset(1))
              .contentEndPosExclusive(asCharOffset(4))
              .referenceStartOffsetInclusive(fromMatchingCharAndEDT(3))
              .referenceEndOffsetInclusive(fromMatchingCharAndEDT(5))
              .build(),
          // CHEESE_WEDGE --> CHEESE_WEDGE (unchanged, region shift due to Unicode)
          new LocatedString.CharacterRegion.Builder()
              .contentNonBmp(true)
              .contentStartPosInclusive(asCharOffset(4))
              .contentEndPosExclusive(asCharOffset(5))
              .referenceStartOffsetInclusive(fromMatchingCharAndEDT(6))
              .referenceEndOffsetInclusive(fromMatchingCharAndEDT(6))
              .build(),
          // ", " --> ", " (unchanged, region shift due to Unicode)
          new LocatedString.CharacterRegion.Builder()
              .contentNonBmp(false)
              .contentStartPosInclusive(asCharOffset(5))
              .contentEndPosExclusive(asCharOffset(7))
              .referenceStartOffsetInclusive(fromMatchingCharAndEDT(7))
              .referenceEndOffsetInclusive(fromMatchingCharAndEDT(8))
              .build(),
          // " " --> FACE_WITH_TEARS_OF_JOY (insertion)
          new LocatedString.CharacterRegion.Builder()
              .contentNonBmp(true)
              .contentStartPosInclusive(asCharOffset(7))
              .contentEndPosExclusive(asCharOffset(8))
              .referenceStartOffsetInclusive(fromMatchingCharAndEDT(8))
              .referenceEndOffsetInclusive(fromMatchingCharAndEDT(8))
              .build(),
          // space insertion
          new LocatedString.CharacterRegion.Builder()
              .contentNonBmp(false)
              .contentStartPosInclusive(asCharOffset(8))
              .contentEndPosExclusive(asCharOffset(9))
              .referenceStartOffsetInclusive(fromMatchingCharAndEDT(8))
              .referenceEndOffsetInclusive(fromMatchingCharAndEDT(8))
              .build(),
          // remainder of the string, unchanged
          new LocatedString.CharacterRegion.Builder()
              .contentNonBmp(false)
              .contentStartPosInclusive(asCharOffset(9))
              .contentEndPosExclusive(asCharOffset(21))
              .referenceStartOffsetInclusive(fromMatchingCharAndEDT(9))
              .referenceEndOffsetInclusive(fromMatchingCharAndEDT(20))
              .build()).build();

  @Test
  public void testGetContent() {
    assertEquals(TEST1_CONTENT, TEST_STRING1.content());
  }

  @Test
  public void testGetReference() {
    assertTrue(TEST_STRING1.referenceString().isPresent());
    assertEquals(TEST1_REFERENCE, TEST_STRING1.referenceString().get());
  }

  @Test
  public void setTest1ReferenceBounds() {
    assertEquals(OffsetGroupRange.from(
        OffsetGroup.fromMatchingCharAndEDT(0), OffsetGroup.fromMatchingCharAndEDT(20)),
        TEST_STRING1.referenceBounds());
  }

  @Test
  public void testStartReferenceOffsetsForContentOffset() {
    // location of CHEESE_WEDGE
    assertEquals(asCharOffset(6),
        TEST_STRING1.startReferenceOffsetsForContentOffset(asCharOffset(4)).charOffset());
    // location of inserted FACE_WITH_TEARS_OF_JOY
    assertEquals(asCharOffset(8),
        TEST_STRING1.startReferenceOffsetsForContentOffset(asCharOffset(7)).charOffset());
    // location of ' replacement
    assertEquals(asCharOffset(0),
        TEST_STRING1.startReferenceOffsetsForContentOffset(asCharOffset(0)).charOffset());
  }

  @Test
  public void testEndReferenceOffsetsForContentOffset() {
    // location of CHEESE_WEDGE
    assertEquals(asCharOffset(6),
        TEST_STRING1.endReferenceOffsetsForContentOffset(asCharOffset(4)).charOffset());
    // location of inserted FACE_WITH_TEARS_OF_JOY
    assertEquals(asCharOffset(8),
        TEST_STRING1.endReferenceOffsetsForContentOffset(asCharOffset(7)).charOffset());
    // location of ' replacement
    assertEquals(asCharOffset(0),
        TEST_STRING1.endReferenceOffsetsForContentOffset(asCharOffset(0)).charOffset());
  }

  private static final LocatedString INITIAL_SUBSTRING1 = new LocatedString.Builder()
      .referenceString(TEST1_REFERENCE)
      .content(unicodeFriendly("'lo " + CHEESE_WEDGE + ","))
      .addCharacterRegions(
          // Hel --> ' (a deletion/replacement)
          new LocatedString.CharacterRegion.Builder()
              .contentNonBmp(false)
              .contentStartPosInclusive(asCharOffset(0))
              .contentEndPosExclusive(asCharOffset(1))
              .referenceStartOffsetInclusive(fromMatchingCharAndEDT(0))
              .referenceEndOffsetInclusive(fromMatchingCharAndEDT(0))
              .build(),
          // "lo " --> "lo " (unchanged)
          new LocatedString.CharacterRegion.Builder()
              .contentNonBmp(false)
              .contentStartPosInclusive(asCharOffset(1))
              .contentEndPosExclusive(asCharOffset(4))
              .referenceStartOffsetInclusive(fromMatchingCharAndEDT(3))
              .referenceEndOffsetInclusive(fromMatchingCharAndEDT(5))
              .build(),
          // CHEESE_WEDGE --> CHEESE_WEDGE (unchanged, region shift due to Unicode)
          new LocatedString.CharacterRegion.Builder()
              .contentNonBmp(true)
              .contentStartPosInclusive(asCharOffset(4))
              .contentEndPosExclusive(asCharOffset(5))
              .referenceStartOffsetInclusive(fromMatchingCharAndEDT(6))
              .referenceEndOffsetInclusive(fromMatchingCharAndEDT(6))
              .build(),
          // "," --> "," (split final region of parent)
          new LocatedString.CharacterRegion.Builder()
              .contentNonBmp(false)
              .contentStartPosInclusive(asCharOffset(5))
              .contentEndPosExclusive(asCharOffset(6))
              .referenceStartOffsetInclusive(fromMatchingCharAndEDT(7))
              .referenceEndOffsetInclusive(fromMatchingCharAndEDT(7))
              .build()).build();
  private static final UnicodeFriendlyString INITIAL_SUBSTRING_REF_STRING =
      unicodeFriendly("Hello " + CHEESE_WEDGE + ",");

  private static final LocatedString FINAL_SUBSTRING1 = new LocatedString.Builder()
      .referenceString(TEST1_REFERENCE)
      .content(unicodeFriendly(" " + FACE_WITH_TEARS_OF_JOY + " how are you?"))
      .addCharacterRegions(
          // " " --> " " (unchanged, region split by substring)
          new LocatedString.CharacterRegion.Builder()
              .contentNonBmp(false)
              .contentStartPosInclusive(asCharOffset(0))
              .contentEndPosExclusive(asCharOffset(1))
              .referenceStartOffsetInclusive(fromMatchingCharAndEDT(8))
              .referenceEndOffsetInclusive(fromMatchingCharAndEDT(8))
              .build(),
          // " " --> FACE_WITH_TEARS_OF_JOY (insertion)
          new LocatedString.CharacterRegion.Builder()
              .contentNonBmp(true)
              .contentStartPosInclusive(asCharOffset(1))
              .contentEndPosExclusive(asCharOffset(2))
              .referenceStartOffsetInclusive(fromMatchingCharAndEDT(8))
              .referenceEndOffsetInclusive(fromMatchingCharAndEDT(8))
              .build(),
          // space insertion
          new LocatedString.CharacterRegion.Builder()
              .contentNonBmp(false)
              .contentStartPosInclusive(asCharOffset(2))
              .contentEndPosExclusive(asCharOffset(3))
              .referenceStartOffsetInclusive(fromMatchingCharAndEDT(8))
              .referenceEndOffsetInclusive(fromMatchingCharAndEDT(8))
              .build(),
          // remainder of the string, unchanged
          new LocatedString.CharacterRegion.Builder()
              .contentNonBmp(false)
              .contentStartPosInclusive(asCharOffset(3))
              .contentEndPosExclusive(asCharOffset(15))
              .referenceStartOffsetInclusive(fromMatchingCharAndEDT(9))
              .referenceEndOffsetInclusive(fromMatchingCharAndEDT(20))
              .build()).build();
  private static final UnicodeFriendlyString FINAL_SUBSTRING_REF_STRING =
      unicodeFriendly(" how are you?");

  private static final LocatedString SPLIT_REGIONS_ON_BOTH_ENDS_SUBSTRING1 =
      new LocatedString.Builder()
          .referenceString(TEST1_REFERENCE)
          .content(unicodeFriendly("o " + CHEESE_WEDGE + ", "
              + FACE_WITH_TEARS_OF_JOY + " how "))
          .addCharacterRegions(
              // "o " --> "o " (unchanged, split by substring)
              new LocatedString.CharacterRegion.Builder()
                  .contentNonBmp(false)
                  .contentStartPosInclusive(asCharOffset(0))
                  .contentEndPosExclusive(asCharOffset(2))
                  .referenceStartOffsetInclusive(fromMatchingCharAndEDT(4))
                  .referenceEndOffsetInclusive(fromMatchingCharAndEDT(5))
                  .build(),
              // CHEESE_WEDGE --> CHEESE_WEDGE (unchanged, region shift due to Unicode)
              new LocatedString.CharacterRegion.Builder()
                  .contentNonBmp(true)
                  .contentStartPosInclusive(asCharOffset(2))
                  .contentEndPosExclusive(asCharOffset(3))
                  .referenceStartOffsetInclusive(fromMatchingCharAndEDT(6))
                  .referenceEndOffsetInclusive(fromMatchingCharAndEDT(6))
                  .build(),
              // ", " --> ", " (unchanged, region shift due to Unicode)
              new LocatedString.CharacterRegion.Builder()
                  .contentNonBmp(false)
                  .contentStartPosInclusive(asCharOffset(3))
                  .contentEndPosExclusive(asCharOffset(5))
                  .referenceStartOffsetInclusive(fromMatchingCharAndEDT(7))
                  .referenceEndOffsetInclusive(fromMatchingCharAndEDT(8))
                  .build(),
              // " " --> FACE_WITH_TEARS_OF_JOY (insertion)
              new LocatedString.CharacterRegion.Builder()
                  .contentNonBmp(true)
                  .contentStartPosInclusive(asCharOffset(5))
                  .contentEndPosExclusive(asCharOffset(6))
                  .referenceStartOffsetInclusive(fromMatchingCharAndEDT(8))
                  .referenceEndOffsetInclusive(fromMatchingCharAndEDT(8))
                  .build(),
              // space insertion
              new LocatedString.CharacterRegion.Builder()
                  .contentNonBmp(false)
                  .contentStartPosInclusive(asCharOffset(6))
                  .contentEndPosExclusive(asCharOffset(7))
                  .referenceStartOffsetInclusive(fromMatchingCharAndEDT(8))
                  .referenceEndOffsetInclusive(fromMatchingCharAndEDT(8))
                  .build(),
              // remainder of the string, unchanged, but split by substring
              new LocatedString.CharacterRegion.Builder()
                  .contentNonBmp(false)
                  .contentStartPosInclusive(asCharOffset(7))
                  .contentEndPosExclusive(asCharOffset(11))
                  .referenceStartOffsetInclusive(fromMatchingCharAndEDT(9))
                  .referenceEndOffsetInclusive(fromMatchingCharAndEDT(12))
                  .build()).build();
  private static final UnicodeFriendlyString SPLIT_REGION_ON_BOTH_ENDS_REF_STRING =
      unicodeFriendly("o " + CHEESE_WEDGE + ", how ");

  private static final LocatedString SINGLE_REGION_SUBSTRING1 = new LocatedString.Builder()
      .referenceString(TEST1_REFERENCE)
      .content(unicodeFriendly(CHEESE_WEDGE))
      .addCharacterRegions(
          // CHEESE_WEDGE --> CHEESE_WEDGE (unchanged, region shift due to Unicode)
          new LocatedString.CharacterRegion.Builder()
              .contentNonBmp(true)
              .contentStartPosInclusive(asCharOffset(0))
              .contentEndPosExclusive(asCharOffset(1))
              .referenceStartOffsetInclusive(fromMatchingCharAndEDT(6))
              .referenceEndOffsetInclusive(fromMatchingCharAndEDT(6))
              .build()).build();
  private static final UnicodeFriendlyString SINGLE_REGION_REFERENCE_STRING =
      unicodeFriendly(CHEESE_WEDGE);


  @Test
  public void testContentLocatedSubstringByContentOffsets() {
    // substring from the beginning, splitting the final character region
    assertEquals(INITIAL_SUBSTRING1, TEST_STRING1.contentLocatedSubstringByContentOffsets(
        OffsetRange.charOffsetRange(0, 5)));
    // substring from middle to end, splitting initial character region
    assertEquals(FINAL_SUBSTRING1, TEST_STRING1.contentLocatedSubstringByContentOffsets(
        OffsetRange.charOffsetRange(6, 20)));
    // substring splitting regions on both ends
    assertEquals(SPLIT_REGIONS_ON_BOTH_ENDS_SUBSTRING1,
        TEST_STRING1.contentLocatedSubstringByContentOffsets(OffsetRange.charOffsetRange(2, 12)));
    // substring which is a single region
    assertEquals(SINGLE_REGION_SUBSTRING1,
        TEST_STRING1.contentLocatedSubstringByContentOffsets(OffsetRange.charOffsetRange(4, 4)));
  }


  @Test
  public void testFromSimpleReferenceString() {
    final UnicodeFriendlyString s = unicodeFriendly("Hello");
    final LocatedString ls = LocatedString.fromReferenceString(s);
    assertEquals(s, ls.content());
    //noinspection OptionalGetWithoutIsPresent
    assertEquals(s, ls.referenceString().get());
    assertEquals(0, ls.referenceBounds().startInclusive().charOffset().asInt());
    assertEquals(4, ls.referenceBounds().endInclusive().charOffset().asInt());
    for (int i = 0; i < 5; ++i) {
      assertEquals(asCharOffset(i),
          ls.startReferenceOffsetsForContentOffset(asCharOffset(i)).charOffset());
    }
  }


  @Test
  public void testReferenceSubstringByContentOffsets() {
    // substring from the beginning, splitting the final character region
    assertEquals(INITIAL_SUBSTRING_REF_STRING, TEST_STRING1.referenceSubstringByContentOffsets(
        OffsetRange.charOffsetRange(0, 5)).get());
    // substring from middle to end, splitting initial character region
    assertEquals(FINAL_SUBSTRING_REF_STRING, TEST_STRING1.referenceSubstringByContentOffsets(
        OffsetRange.charOffsetRange(6, 20)).get());
    // substring splitting regions on both ends
    assertEquals(SPLIT_REGION_ON_BOTH_ENDS_REF_STRING,
        TEST_STRING1.referenceSubstringByContentOffsets(OffsetRange.charOffsetRange(2, 12)).get());
    // substring which is a single region
    assertEquals(SINGLE_REGION_REFERENCE_STRING,
        TEST_STRING1.referenceSubstringByContentOffsets(OffsetRange.charOffsetRange(4, 4)).get());
  }

  @Test
  public void testContainsExactly() {
    assertTrue(TEST_STRING1.containsExactly(INITIAL_SUBSTRING1));
    assertTrue(TEST_STRING1.containsExactly(FINAL_SUBSTRING1));
    assertTrue(TEST_STRING1.containsExactly(SPLIT_REGIONS_ON_BOTH_ENDS_SUBSTRING1));
    assertTrue(TEST_STRING1.containsExactly(SINGLE_REGION_SUBSTRING1));
  }

  @Test
  public void testReferenceBounds() {
    assertEquals(0, TEST_STRING1.referenceBounds().startCharOffsetInclusive().asInt());
    assertEquals(20, TEST_STRING1.referenceBounds().endCharOffsetInclusive().asInt());
  }

  @Test
  public void simpleSubstring() {
    final UnicodeFriendlyString s = unicodeFriendly("Hello");
    final LocatedString ls = LocatedString.fromReferenceString(s);

    // substring covering all
    final LocatedString hello = ls.contentLocatedSubstringByContentOffsets(
        OffsetRange.charOffsetRange(0, s.lengthInCodePoints() - 1));
    assertEquals("Hello", hello.content().utf16CodeUnits());
    assertEquals(hello.referenceString().get(), ls.referenceString().get());
    for (int i = 0; i < 5; ++i) {
      assertEquals(ls.startReferenceOffsetsForContentOffset(asCharOffset(i)),
          hello.startReferenceOffsetsForContentOffset(asCharOffset(i)));
    }

    // substring starting from the beginning
    final LocatedString hel =
        ls.contentLocatedSubstringByContentOffsets(OffsetRange.charOffsetRange(0, 2));
    assertEquals("Hel", hel.content().utf16CodeUnits());
    assertEquals(ls.referenceString().get(), hel.referenceString().get());
    for (int i = 0; i < 3; ++i) {
      assertEquals(ls.startReferenceOffsetsForContentOffset(asCharOffset(i)),
          hel.startReferenceOffsetsForContentOffset(asCharOffset(i)));
    }

    // substring touching the end
    final LocatedString lo =
        ls.contentLocatedSubstringByContentOffsets(OffsetRange.charOffsetRange(3, 4));
    assertEquals("lo", lo.content().utf16CodeUnits());
    assertEquals(ls.referenceString().get(), lo.referenceString().get());
    for (int i = 0; i < 2; ++i) {
      assertEquals(ls.startReferenceOffsetsForContentOffset(asCharOffset(3 + i)),
          lo.startReferenceOffsetsForContentOffset(asCharOffset(i)));
    }

    // middle substring
    final LocatedString ell =
        ls.contentLocatedSubstringByContentOffsets(OffsetRange.charOffsetRange(1, 3));
    assertEquals("ell", ell.content().utf16CodeUnits());
    assertEquals(ls.referenceString().get(), ell.referenceString().get());
    for (int i = 0; i < 3; ++i) {
      assertEquals(ls.startReferenceOffsetsForContentOffset(asCharOffset(1 + i)),
          ell.startReferenceOffsetsForContentOffset(asCharOffset(i)));
    }
  }
}
