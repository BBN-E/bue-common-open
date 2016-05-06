package com.bbn.bue.common.strings;

import com.bbn.bue.common.StringUtils;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link StringUtils}.
 */
public class StringUtilsTest {

  public static final String FOO = "foo";
  public static final String GREEK_FOO = "Φου";
  public static final String NON_BMP = "\uD83D\uDE02\uD83D\uDFB2";

  @Test
  public void testPadding() {
    assertEquals("0", StringUtils.padWithMax(0, 0));
    assertEquals("1", StringUtils.padWithMax(1, 1));
    assertEquals("1", StringUtils.padWithMax(1, 9));
    assertEquals("01", StringUtils.padWithMax(1, 10));
    assertEquals("05", StringUtils.padWithMax(5, 99));
    assertEquals("005", StringUtils.padWithMax(5, 100));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPaddingNegException() {
    StringUtils.padWithMax(-1, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPaddingBadMaxException() {
    StringUtils.padWithMax(10, 3);
  }

  @Test
  public void testToCodepoints() {
    assertEquals(ImmutableList.of(102, 111, 111),
        StringUtils.toCodepoints(FOO));
    assertEquals(ImmutableList.of(934, 959, 965),
        StringUtils.toCodepoints(GREEK_FOO));
    assertEquals(ImmutableList.of(128514, 128946),
        StringUtils.toCodepoints(NON_BMP));
  }

  @Test
  public void testToCodepointStrings() {
    assertEquals(ImmutableList.of("f", "o", "o"),
        StringUtils.toCodepointStrings(FOO));
    assertEquals(ImmutableList.of("Φ", "ο", "υ"),
        StringUtils.toCodepointStrings(GREEK_FOO));
    assertEquals(ImmutableList.of("\uD83D\uDE02", "\uD83D\uDFB2"),
        StringUtils.toCodepointStrings(NON_BMP));
  }
}
