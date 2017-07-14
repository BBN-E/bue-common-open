package com.bbn.bue.common;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by rgabbard on 7/13/17.
 */
public class ExplicitOrderingNonExclusiveTest {

  private final List<String> explicitOrder = ImmutableList.of("b", "c", "d", "e");
  private final List<String> sampleData = ImmutableList.of("z", "d", "f", "b", "c", "g");

  @Test
  public void testUnrankedLarger() {
    assertEquals(ImmutableList.of("b", "c", "d", "z", "f", "g"),
        OrderingUtils.explicitOrderingNonExclusiveUnrankedLarger(explicitOrder)
            .immutableSortedCopy(sampleData));
    assertEquals(ImmutableList.of("b", "c", "d", "z", "f", "g"),
        OrderingUtils.explicitOrderingNonExclusiveUnrankedLarger("b", "c", "d", "e")
            .immutableSortedCopy(sampleData));
  }

  @Test
  public void testUnrankedSmaller() {
    assertEquals(ImmutableList.of("z", "f", "g", "b", "c", "d"),
        OrderingUtils.explicitOrderingNonExclusiveUnrankedSmaller(explicitOrder)
            .immutableSortedCopy(sampleData));
    assertEquals(ImmutableList.of("z", "f", "g", "b", "c", "d"),
        OrderingUtils.explicitOrderingNonExclusiveUnrankedSmaller("b", "c", "d", "e")
            .immutableSortedCopy(sampleData));
  }
}
