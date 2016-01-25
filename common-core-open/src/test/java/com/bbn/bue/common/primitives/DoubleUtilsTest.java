package com.bbn.bue.common.primitives;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by rgabbard on 1/25/16.
 */
public class DoubleUtilsTest {

  private static final double EPSILON = 1e-6;

  @Test(expected = IllegalArgumentException.class)
  public void testBadBounds() {
    DoubleUtils.clip(2.0, -1.0);
  }

  @Test
  public void testClipping() {
    assertEquals(-2.0, DoubleUtils.clip(-5.0, 2.0), EPSILON);
    assertEquals(2.0, DoubleUtils.clip(5.0, 2.0), EPSILON);
    assertEquals(1.0, DoubleUtils.clip(1.0, 2.0), EPSILON);
  }
}
