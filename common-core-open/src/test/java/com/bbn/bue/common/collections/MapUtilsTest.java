package com.bbn.bue.common.collections;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public final class MapUtilsTest {

  @Test
  public void testCopyWithSortedKeys() {
    final Map<String, String> input = Maps.newHashMap();
    input.put("hello", "world");
    input.put("abc", "zzz");
    input.put("zoo", "gorilla");

    final ImmutableMap<String, String> reference = ImmutableMap.of(
        "abc", "zzz",
        "hello", "world",
        "zoo", "gorilla");

    assertEquals(reference, MapUtils.copyWithSortedKeys(input));

    final ImmutableMap<String, String> reversedReference = ImmutableMap.of(
        "zoo", "gorilla",
        "hello", "world",
        "abc", "zzz");

    assertEquals(reversedReference, MapUtils.copyWithKeysSortedBy(input,
        Ordering.natural().reverse()));
  }
}
