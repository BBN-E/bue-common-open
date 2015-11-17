package com.bbn.bue.common.collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import org.junit.Test;

import java.util.Collection;

import static com.bbn.bue.common.collections.CollectionUtils.partitionAlmostEvenly;
import static org.junit.Assert.assertEquals;

/**
 * Tests the CollectionUtils class.
 */
public final class CollectionUtilsTest {

  @Test
  public void testPartition() {
    assertEquals(
        ImmutableList.of(
            ImmutableList.of(1, 2)),
        partitionAlmostEvenly(ImmutableList.of(1, 2), 1));
    assertEquals(
        ImmutableList.of(
            ImmutableList.of(1),
            ImmutableList.of(2)),
        partitionAlmostEvenly(ImmutableList.of(1, 2), 2));
    assertEquals(
        ImmutableList.of(
            ImmutableList.of(1, 3),
            ImmutableList.of(2)),
        partitionAlmostEvenly(ImmutableList.of(1, 2, 3), 2));
    assertEquals(
        ImmutableList.of(
            ImmutableList.of(1, 2),
            ImmutableList.of(3, 4)),
        partitionAlmostEvenly(ImmutableList.of(1, 2, 3, 4), 2));
    assertEquals(
        ImmutableList.of(
            ImmutableList.of(1, 4),
            ImmutableList.of(2),
            ImmutableList.of(3)),
        partitionAlmostEvenly(ImmutableList.of(1, 2, 3, 4), 3));
    assertEquals(
        ImmutableList.of(
            ImmutableList.of(1, 2, 3, 10),
            ImmutableList.of(4, 5, 6),
            ImmutableList.of(7, 8, 9)),
        partitionAlmostEvenly(ImmutableList.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), 3));
  }

  @Test(expected = NullPointerException.class)
  public void testPartitionArgsCheck1() {
    partitionAlmostEvenly(null, 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPartitionArgsCheck2() {
    partitionAlmostEvenly(ImmutableList.of(), 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPartitionArgsCheck3() {
    partitionAlmostEvenly(ImmutableList.of(1), 2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPartitionArgsCheck4() {
    partitionAlmostEvenly(ImmutableList.of(1), 0);
  }

  private final ImmutableSet<String> left = ImmutableSet.of("hello", "world", "!");
  private final ImmutableList<String> right = ImmutableList.of("this", "is", "a", "list");
  private final ImmutableList<String> reference = ImmutableList.of("hello", "world", "!",
      "this", "is", "a", "list");

  @Test
  public void testConcat() {
    final Collection<String> concatted = CollectionUtils.concat(left, right);
    assertEquals(7, concatted.size());
    for (int i = 0; i < reference.size(); ++i) {
      assertEquals(reference.get(i), Iterables.get(concatted, i));
    }
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testConcatOutOfBounds() {
    final Collection<String> concatted = CollectionUtils.concat(left, right);
    Iterables.get(concatted, reference.size());
  }
}
