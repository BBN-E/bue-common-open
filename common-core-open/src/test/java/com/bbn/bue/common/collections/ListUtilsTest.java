package com.bbn.bue.common.collections;

import com.google.common.collect.Lists;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ListUtilsTest {

  @Test
  public void testListConcat() {
    final List<String> list1 = Lists.newArrayList("Hello", "world");
    final List<String> list2 = Lists.newArrayList("foo", "bar", "baz");
    final List<String> combined = ListUtils.concat(list1, list2);

    assertEquals(Lists.newArrayList("Hello", "world", "foo", "bar", "baz"),
        combined);
    list1.add("new element");
    assertEquals(Lists.newArrayList("Hello", "world", "new element", "foo", "bar", "baz"),
        combined);
  }
}
