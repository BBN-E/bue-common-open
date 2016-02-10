package com.bbn.bue.common.collections;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

import org.junit.Test;

import java.util.Map;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
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

  @Test
  public void testAllowSameEntry() {
    final LaxImmutableMapBuilder<String, String> ret =
        MapUtils.immutableMapBuilderAllowingSameEntryTwice();
    ret.put("hello", "world");
    ret.put("hello", "world");
    ret.build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAllowSameEntryFail() {
    final LaxImmutableMapBuilder<String, String> ret =
        MapUtils.immutableMapBuilderAllowingSameEntryTwice();
    ret.put("hello", "world");
    ret.put("hello", "earth");
    ret.build();
  }

  @Test
  public void testKeepFirst() {
    final LaxImmutableMapBuilder<String, String> ret =
        MapUtils.immutableMapBuilderIgnoringDuplicates();
    ret.put("hello", "world");
    ret.put("hello", "earth");
    final ImmutableMap<String, String> map = ret.build();
    assertEquals(1, map.size());
    assertEquals("world", map.get("hello"));
  }

  private static final Ordering<String> BY_REVERSE_STRING =
      Ordering.natural().onResultOf(new Function<String, String>() {
        @Override
        public String apply(@Nullable final String s) {
          return new StringBuilder(checkNotNull(s)).reverse().toString();
        }
      });

  @Test
  public void testBestByComparator() {
    final LaxImmutableMapBuilder<String, String> ret =
        MapUtils.immutableMapBuilderResolvingDuplicatesBy(BY_REVERSE_STRING);
    ret.put("hello", "world");
    ret.put("hello", "earth");
    final ImmutableMap<String, String> map = ret.build();
    assertEquals(1, map.size());
    assertEquals("earth", map.get("hello"));
  }
}
