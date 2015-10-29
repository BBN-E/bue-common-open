package com.bbn.bue.common.strings;

import com.bbn.bue.common.StringUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by rgabbard on 10/28/15.
 */
public class StringUtilsTest {

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
}
