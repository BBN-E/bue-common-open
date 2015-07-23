package com.bbn.bue.common.strings;

import com.google.common.base.Optional;

import org.junit.Test;

import static com.google.common.base.Preconditions.checkArgument;
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
