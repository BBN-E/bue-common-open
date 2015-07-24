package com.bbn.bue.common.strings;

import com.bbn.bue.common.strings.offsets.CharOffset;
import com.bbn.bue.common.strings.offsets.EDTOffset;
import com.bbn.bue.common.strings.offsets.OffsetGroup;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import org.junit.Test;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by jdeyoung on 7/7/15.
 */
public final class LocatedStringTest {

  private final static String partOne = "This is a very silly";
  private final static String partTwo = "look! another string, with new lines this time! \n"
//      + "\n\r\n"
      + " The quick fox jumped over the lazy brown dog";
  private final static String base =
      "<TAG>" + partOne + "<string with=self closing=tags/> <FNORD/> " + partTwo + " </TAG>";
  private final static String before = "text before xml ";
  private final static String plusBase = before + base;
  private final static String after = "test after xml";
  private final static String basePlus = base + after;

  private void stringContainsKnownSubStrings(final LocatedString complete) {
    assertTrue(deriveSubstring(complete, partOne).isPresent());
    assertTrue(deriveSubstring(complete, partTwo).isPresent());
  }

  @Test
  public void substringTest() {
    final String baseString = "H<b>ello</b> world";
    final LocatedString locatedString = LocatedString.forString(baseString);
    final List<OffsetGroup> posesInString = ImmutableList.of(
        OffsetGroup.from(CharOffset.asCharOffset(0), EDTOffset.asEDTOffset(0)),
        OffsetGroup.from(CharOffset.asCharOffset(1), EDTOffset.asEDTOffset(0)),
        OffsetGroup.from(CharOffset.asCharOffset(2), EDTOffset.asEDTOffset(0)),
        OffsetGroup.from(CharOffset.asCharOffset(3), EDTOffset.asEDTOffset(0)),
        OffsetGroup.from(CharOffset.asCharOffset(4), EDTOffset.asEDTOffset(1)),
        OffsetGroup.from(CharOffset.asCharOffset(5), EDTOffset.asEDTOffset(2)),
        OffsetGroup.from(CharOffset.asCharOffset(6), EDTOffset.asEDTOffset(3)),
        OffsetGroup.from(CharOffset.asCharOffset(7), EDTOffset.asEDTOffset(4)),
        OffsetGroup.from(CharOffset.asCharOffset(8), EDTOffset.asEDTOffset(4)),
        OffsetGroup.from(CharOffset.asCharOffset(9), EDTOffset.asEDTOffset(4)),
        OffsetGroup.from(CharOffset.asCharOffset(10), EDTOffset.asEDTOffset(4)),
        OffsetGroup.from(CharOffset.asCharOffset(11), EDTOffset.asEDTOffset(4)),
        OffsetGroup.from(CharOffset.asCharOffset(12), EDTOffset.asEDTOffset(5)),
        OffsetGroup.from(CharOffset.asCharOffset(13), EDTOffset.asEDTOffset(6)),
        OffsetGroup.from(CharOffset.asCharOffset(14), EDTOffset.asEDTOffset(7)),
        OffsetGroup.from(CharOffset.asCharOffset(15), EDTOffset.asEDTOffset(8)),
        OffsetGroup.from(CharOffset.asCharOffset(16), EDTOffset.asEDTOffset(9)),
        OffsetGroup.from(CharOffset.asCharOffset(17), EDTOffset.asEDTOffset(10)));

    for (final OffsetGroup start : posesInString) {
      for (final OffsetGroup end : posesInString) {
        if (start.charOffset().value() < end.charOffset().value()) {
          final LocatedString substring = locatedString.substring(start, end);
          assertEquals(start.charOffset(), substring.startCharOffset());
          assertEquals(end.charOffset(), substring.endCharOffset());
          assertEquals(start.edtOffset(), substring.startEDTOffset());
          assertEquals(end.edtOffset(), substring.endEDTOffset());
        }
      }
    }
  }

  @Test
  public void baseContainsInterior() {
    final LocatedString complete = LocatedString.forString(base);
    stringContainsKnownSubStrings(complete);
  }

  @Test
  public void correctlyHandleStartingStrings() {
    final LocatedString complete = LocatedString.forString(plusBase);
    assertTrue(deriveSubstring(complete, before).isPresent());
    stringContainsKnownSubStrings(complete);
  }

  @Test
  public void correctHandleEndingStrings() {
    final LocatedString complete = LocatedString.forString(basePlus);
    assertTrue(deriveSubstring(complete, after).isPresent());
    stringContainsKnownSubStrings(complete);
  }


  private static Optional<LocatedString> deriveSubstring(final LocatedString base,
      final String component) {
    int loc = base.text().indexOf(component);
    checkArgument(loc >= 0);
    final LocatedString substring = base.substring(loc, loc + component.length());
    if (base.contains(substring)) {
      return Optional.of(substring);
    }
    return Optional.absent();
  }
}
