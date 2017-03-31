package com.bbn.bue.common;

import com.bbn.bue.common.strings.offsets.OffsetRange;

import org.junit.Test;

import static com.bbn.bue.common.StringUtils.unicodeFriendly;
import static com.bbn.bue.common.strings.offsets.CharOffset.asCharOffset;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UnicodeFriendlyStringTest {

  // non-BMP emoji
  private final String CHEESE_WEDGE = "\uD83E\uDDC0";
  private static final String FACE_WITH_TEARS_OF_JOY = "\uD83D\uDE02";

  private final String EMPTY_CODEUNITS = "";

  private final UnicodeFriendlyString EMPTY = unicodeFriendly(EMPTY_CODEUNITS);
  private final String HELLO_WORLD_CODEUNITS = "Hello world!";
  private final UnicodeFriendlyString HELLO_WORLD = unicodeFriendly(
      HELLO_WORLD_CODEUNITS);
  private final String HELLO_CHEESE_WEDGE_CODEUNITS = "Hello " + CHEESE_WEDGE + "!";
  private final UnicodeFriendlyString HELLO_CHEESE_WEDGE =
      unicodeFriendly(HELLO_CHEESE_WEDGE_CODEUNITS);
  private final String CHEESE_WEDGE_AND_TEARS_CODEUNITS = CHEESE_WEDGE + FACE_WITH_TEARS_OF_JOY;
  private final UnicodeFriendlyString CHEESE_WEDGE_AND_TEARS =
      unicodeFriendly(CHEESE_WEDGE_AND_TEARS_CODEUNITS);
  private final String HELLO_CHEESE_WEDGE_TEARS_CODEUNITS =
      "Hello " + CHEESE_WEDGE + FACE_WITH_TEARS_OF_JOY + "!";
  private final UnicodeFriendlyString HELLO_CHEESE_WEDGE_TEARS =
      unicodeFriendly(HELLO_CHEESE_WEDGE_TEARS_CODEUNITS);

  @Test
  public void testHasNonBmpCharacter() {
    assertFalse(EMPTY.hasNonBmpCharacter());
    assertFalse(HELLO_WORLD.hasNonBmpCharacter());
    assertTrue(unicodeFriendly(CHEESE_WEDGE).hasNonBmpCharacter());
    assertTrue(unicodeFriendly(CHEESE_WEDGE + FACE_WITH_TEARS_OF_JOY).hasNonBmpCharacter());
    assertTrue(unicodeFriendly(FACE_WITH_TEARS_OF_JOY).hasNonBmpCharacter());
    assertTrue(HELLO_CHEESE_WEDGE.hasNonBmpCharacter());
    assertTrue(HELLO_CHEESE_WEDGE_TEARS.hasNonBmpCharacter());
  }

  @Test
  public void testHasNonBmpCharacterRange() {
    assertFalse(HELLO_WORLD.hasNonBmpCharacter(OffsetRange.charOffsetRange(0, 4)));
    // hello
    assertFalse(HELLO_CHEESE_WEDGE.hasNonBmpCharacter(OffsetRange.charOffsetRange(0, 4)));
    // !
    assertFalse(HELLO_CHEESE_WEDGE.hasNonBmpCharacter(OffsetRange.charOffsetRange(7, 7)));
    // CHEESE_WEDGE! - non-BMP at beginning
    assertTrue(HELLO_CHEESE_WEDGE.hasNonBmpCharacter(OffsetRange.charOffsetRange(6, 7)));
    // o CHEESE_WEDGE - non-BMP at end
    assertTrue(HELLO_CHEESE_WEDGE.hasNonBmpCharacter(OffsetRange.charOffsetRange(4, 6)));
    // o CHEESE_WEDGE! - non-BMP in middle
    assertTrue(HELLO_CHEESE_WEDGE.hasNonBmpCharacter(OffsetRange.charOffsetRange(4, 7)));

    // for a region with multiple adjacent non-BMP characters
    assertTrue(CHEESE_WEDGE_AND_TEARS.hasNonBmpCharacter());
    assertTrue(CHEESE_WEDGE_AND_TEARS.hasNonBmpCharacter(OffsetRange.charOffsetRange(0, 0)));
    assertTrue(CHEESE_WEDGE_AND_TEARS.hasNonBmpCharacter(OffsetRange.charOffsetRange(1, 1)));
    // covers transitions between regions with non-bmp, regions with multiple bmp, and regions with non-bmp.
    // hello
    assertFalse(HELLO_CHEESE_WEDGE_TEARS.hasNonBmpCharacter(OffsetRange.charOffsetRange(0, 4)));
    // !
    assertFalse(HELLO_CHEESE_WEDGE_TEARS.hasNonBmpCharacter(OffsetRange.charOffsetRange(8, 8)));
    // CHEESE_WEDGE + FACE_WITH_TEARS_OF_JOY !- non-BMP at beginning
    assertTrue(HELLO_CHEESE_WEDGE_TEARS.hasNonBmpCharacter(OffsetRange.charOffsetRange(6, 8)));
    // CHEESE_WEDGE + FACE_WITH_TEARS_OF_JOY - non-BMP at end
    assertTrue(HELLO_CHEESE_WEDGE_TEARS.hasNonBmpCharacter(OffsetRange.charOffsetRange(4, 7)));
    // CHEESE_WEDGE + FACE_WITH_TEARS_OF_JOY ! - non-BMP in middle
    assertTrue(HELLO_CHEESE_WEDGE_TEARS.hasNonBmpCharacter(OffsetRange.charOffsetRange(4, 8)));
  }

  @Test
  public void testUtf16CodePoints() {
    assertEquals(EMPTY_CODEUNITS, EMPTY.utf16CodeUnits());
    assertEquals(HELLO_WORLD_CODEUNITS, HELLO_WORLD.utf16CodeUnits());
    assertEquals(CHEESE_WEDGE_AND_TEARS_CODEUNITS, CHEESE_WEDGE_AND_TEARS.utf16CodeUnits());
    assertEquals(HELLO_CHEESE_WEDGE_CODEUNITS, HELLO_CHEESE_WEDGE.utf16CodeUnits());
    assertEquals(HELLO_CHEESE_WEDGE_TEARS_CODEUNITS, HELLO_CHEESE_WEDGE_TEARS.utf16CodeUnits());
  }

  @Test
  public void testLengthInUtf16CodeUnits() {
    assertEquals(EMPTY_CODEUNITS.length(), EMPTY.lengthInUtf16CodeUnits());
    assertEquals(HELLO_WORLD_CODEUNITS.length(), HELLO_WORLD.lengthInUtf16CodeUnits());
    assertEquals(CHEESE_WEDGE_AND_TEARS_CODEUNITS.length(),
        CHEESE_WEDGE_AND_TEARS.lengthInUtf16CodeUnits());
    assertEquals(HELLO_CHEESE_WEDGE_CODEUNITS.length(),
        HELLO_CHEESE_WEDGE.lengthInUtf16CodeUnits());
    assertEquals(HELLO_CHEESE_WEDGE_TEARS_CODEUNITS.length(),
        HELLO_CHEESE_WEDGE_TEARS.lengthInUtf16CodeUnits());
  }

  @Test
  public void testLengthInCodePoints() {
    assertEquals(0, EMPTY.lengthInCodePoints());
    assertEquals(12, HELLO_WORLD.lengthInCodePoints());
    assertEquals(2, CHEESE_WEDGE_AND_TEARS.lengthInCodePoints());
    assertEquals(8, HELLO_CHEESE_WEDGE.lengthInCodePoints());
    assertEquals(9, HELLO_CHEESE_WEDGE_TEARS.lengthInCodePoints());
  }

  @Test
  public void testSubstringByCodePoints() {
    // substring tests with start offset only
    assertEquals(unicodeFriendly("world!"), HELLO_WORLD.substringByCodePoints(
        asCharOffset(6)));
    // start substring past non-BMP
    assertEquals(unicodeFriendly("!"), HELLO_CHEESE_WEDGE.substringByCodePoints(
        asCharOffset(7)));
    assertEquals(unicodeFriendly("!"),
        HELLO_CHEESE_WEDGE_TEARS.substringByCodePoints(asCharOffset(8)));
    // start substring before non-BMP
    assertEquals(unicodeFriendly(CHEESE_WEDGE + "!"), HELLO_CHEESE_WEDGE.substringByCodePoints(
        asCharOffset(6)));
    assertEquals(unicodeFriendly(CHEESE_WEDGE_AND_TEARS_CODEUNITS + "!"),
        HELLO_CHEESE_WEDGE_TEARS.substringByCodePoints(asCharOffset(6)));
    // start substring at non-BMP
    assertEquals(unicodeFriendly("o " + CHEESE_WEDGE + "!"),
        HELLO_CHEESE_WEDGE.substringByCodePoints(asCharOffset(4)));
    assertEquals(unicodeFriendly("o " + CHEESE_WEDGE_AND_TEARS_CODEUNITS + "!"),
        HELLO_CHEESE_WEDGE_TEARS.substringByCodePoints(asCharOffset(4)));


    // substring test with start and end offsets
    assertEquals(unicodeFriendly("worl"), HELLO_WORLD.substringByCodePoints(
        asCharOffset(6), asCharOffset(10)));
    // start substring past non-BMP
    assertEquals(unicodeFriendly("!"), HELLO_CHEESE_WEDGE.substringByCodePoints(
        asCharOffset(7), asCharOffset(8)));
    assertEquals(unicodeFriendly("!"), HELLO_CHEESE_WEDGE_TEARS.substringByCodePoints(
        asCharOffset(8), asCharOffset(9)));
    // start substring before non-BMP
    assertEquals(unicodeFriendly(CHEESE_WEDGE), HELLO_CHEESE_WEDGE.substringByCodePoints(
        asCharOffset(6), asCharOffset(7)));
    assertEquals(CHEESE_WEDGE_AND_TEARS, HELLO_CHEESE_WEDGE_TEARS.substringByCodePoints(
        asCharOffset(6), asCharOffset(8)));
    // start substring at non-BMP
    assertEquals(unicodeFriendly("o " + CHEESE_WEDGE),
        HELLO_CHEESE_WEDGE.substringByCodePoints(asCharOffset(4), asCharOffset(7)));
    assertEquals(unicodeFriendly("o " + CHEESE_WEDGE_AND_TEARS_CODEUNITS),
        HELLO_CHEESE_WEDGE_TEARS.substringByCodePoints(asCharOffset(4), asCharOffset(8)));

    // substring test with character offset ranges
    assertEquals(unicodeFriendly("worl"), HELLO_WORLD.substringByCodePoints(
        OffsetRange.charOffsetRange(6, 9)));
    // start substring past non-BMP
    assertEquals(unicodeFriendly("!"), HELLO_CHEESE_WEDGE.substringByCodePoints(
        OffsetRange.charOffsetRange(7, 7)));
    assertEquals(unicodeFriendly("!"), HELLO_CHEESE_WEDGE_TEARS.substringByCodePoints(
        OffsetRange.charOffsetRange(8, 8)));
    // start substring before non-BMP
    assertEquals(unicodeFriendly(CHEESE_WEDGE), HELLO_CHEESE_WEDGE.substringByCodePoints(
        OffsetRange.charOffsetRange(6, 6)));
    assertEquals(CHEESE_WEDGE_AND_TEARS, HELLO_CHEESE_WEDGE_TEARS.substringByCodePoints(
        OffsetRange.charOffsetRange(6, 7)));
    // start substring at non-BMP
    assertEquals(unicodeFriendly("o " + CHEESE_WEDGE),
        HELLO_CHEESE_WEDGE.substringByCodePoints(OffsetRange.charOffsetRange(4, 6)));
    assertEquals(unicodeFriendly("o " + CHEESE_WEDGE_AND_TEARS_CODEUNITS),
        HELLO_CHEESE_WEDGE_TEARS.substringByCodePoints(OffsetRange.charOffsetRange(4, 7)));
  }

  @Test
  public void testIsEmpty() {
    assertTrue(EMPTY.isEmpty());
    assertFalse(HELLO_WORLD.isEmpty());
    assertFalse(CHEESE_WEDGE_AND_TEARS.isEmpty());
    assertFalse(HELLO_CHEESE_WEDGE.isEmpty());
    assertFalse(HELLO_CHEESE_WEDGE_TEARS.isEmpty());
  }

  @Test
  public void testTrim() {
    // doesn't need trimming
    assertEquals(EMPTY, EMPTY.trim());
    assertEquals(HELLO_WORLD, HELLO_WORLD.trim());
    assertEquals(CHEESE_WEDGE_AND_TEARS, CHEESE_WEDGE_AND_TEARS.trim());
    assertEquals(HELLO_CHEESE_WEDGE, HELLO_CHEESE_WEDGE.trim());
    assertEquals(HELLO_CHEESE_WEDGE_TEARS, HELLO_CHEESE_WEDGE_TEARS.trim());

    // does need trimming, no non-BMP
    assertEquals(unicodeFriendly("world!"),
        HELLO_WORLD.substringByCodePoints(asCharOffset(5)).trim());
    // does need trimming, has non-BMP
    assertEquals(unicodeFriendly(CHEESE_WEDGE),
        HELLO_CHEESE_WEDGE.substringByCodePoints(asCharOffset(5), asCharOffset(7)).trim());
    // does need trimming, has non-BMP
    assertEquals(CHEESE_WEDGE_AND_TEARS,
        HELLO_CHEESE_WEDGE_TEARS.substringByCodePoints(asCharOffset(5), asCharOffset(8)).trim());
  }

  @Test
  public void testBmpLessSubstringOfBmpful() {
    assertFalse(HELLO_CHEESE_WEDGE.substringByCodePoints(asCharOffset(0),
        asCharOffset(3)).hasNonBmpCharacter());
    assertFalse(HELLO_CHEESE_WEDGE_TEARS.substringByCodePoints(asCharOffset(0),
        asCharOffset(3)).hasNonBmpCharacter());
  }
}
