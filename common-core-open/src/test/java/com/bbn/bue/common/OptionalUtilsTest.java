package com.bbn.bue.common;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import org.junit.Test;

public class OptionalUtilsTest {

  final Optional<String> a = Optional.absent();
  final Optional<String> b = Optional.absent();
  final Optional<String> c = Optional.of("c");
  final Optional<String> d = Optional.of("d");
  final Optional<Integer> e = Optional.absent();
  final Optional<Integer> f = Optional.of(2);

  @Test
  public void testSuccess() {
    OptionalUtils.exactlyOnePresentOrIllegalState(ImmutableList.of(a, b, c, e), "foo");
    OptionalUtils.exactlyOnePresentOrIllegalState(ImmutableList.of(b, e, f), "foo");
    OptionalUtils.exactlyOnePresentOrIllegalState(ImmutableList.of(a, b, c), "foo");

    OptionalUtils.exactlyOnePresentOrIllegalArgument(ImmutableList.of(a, b, c, e), "foo");
    OptionalUtils.exactlyOnePresentOrIllegalArgument(ImmutableList.of(b, e, f), "foo");
    OptionalUtils.exactlyOnePresentOrIllegalArgument(ImmutableList.of(a, b, c), "foo");
  }

  @Test(expected = IllegalStateException.class)
  public void testFailure1() {
    OptionalUtils.exactlyOnePresentOrIllegalState(ImmutableList.of(c, d, e), "foo");
  }

  @Test(expected = IllegalStateException.class)
  public void testFailure2() {
    OptionalUtils.exactlyOnePresentOrIllegalState(ImmutableList.of(a, b, e), "foo");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFailure3() {
    OptionalUtils.exactlyOnePresentOrIllegalArgument(ImmutableList.of(c, d, e), "foo");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFailure4() {
    OptionalUtils.exactlyOnePresentOrIllegalArgument(ImmutableList.of(a, b, e), "foo");
  }
}
