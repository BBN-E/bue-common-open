package com.bbn.nlp.clustering.visualization;

import com.google.common.collect.ImmutableSet;

import org.junit.Test;

import java.util.Set;

public class LinearAlignerTest {

  @Test
  public void testLinearAligner() {
    final LinearAligner<String> aligner = LinearAligner.createWithLinearPenalty(false);
    final Set<ImmutableSet<String>> left = ImmutableSet.of(
        ImmutableSet.of("a", "b", "q"),
        ImmutableSet.of("a", "c", "d"),
        ImmutableSet.of("x", "a"));
    final Set<ImmutableSet<String>> right = ImmutableSet.of(
        ImmutableSet.of("c", "d", "x"),
        ImmutableSet.of("a", "b"),
        ImmutableSet.of("q"),
        ImmutableSet.of("x", "d"));

    final LinearAligner.Result<String> result = aligner.align(left, right, 10);

    System.out.println(result);
  }
}
