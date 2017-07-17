package com.bbn.bue.common;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ExplicitOrderingNonExclusiveTest {

  private final List<String> explicitOrder = ImmutableList.of("b", "c", "d", "e");
  private final List<String> sampleData = ImmutableList.of("z", "d", "f", "b", "c", "g");

  @Test
  public void testUnrankedLast() {
    assertEquals(ImmutableList.of("b", "c", "d", "z", "f", "g"),
        OrderingUtils.explicitOrderingUnrankedLast(explicitOrder)
            .immutableSortedCopy(sampleData));
    assertEquals(ImmutableList.of("b", "c", "d", "z", "f", "g"),
        OrderingUtils.explicitOrderingUnrankedLast("b", "c", "d", "e")
            .immutableSortedCopy(sampleData));
  }

  @Test
  public void testUnrankedFirst() {
    assertEquals(ImmutableList.of("z", "f", "g", "b", "c", "d"),
        OrderingUtils.explicitOrderingUnrankedFirst(explicitOrder)
            .immutableSortedCopy(sampleData));
    assertEquals(ImmutableList.of("z", "f", "g", "b", "c", "d"),
        OrderingUtils.explicitOrderingUnrankedFirst("b", "c", "d", "e")
            .immutableSortedCopy(sampleData));
  }
}
