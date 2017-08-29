package com.bbn.bue.common.hppc;

import com.carrotsearch.hppc.ObjectIntHashMap;
import com.google.common.collect.ImmutableSet;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HppcUtilsTest {

  @Test
  public void objectCollectionAsIterableTest() {
    ObjectIntHashMap<String> foo = new ObjectIntHashMap<>();
    foo.put("foo", 1);
    foo.put("foo2", 2);
    foo.put("bar", 8);

    final ImmutableSet<String> reference = ImmutableSet.of("foo", "foo2", "bar");
    assertEquals(reference, ImmutableSet.copyOf(HppcUtils.asJavaIterable(foo.keys())));
  }
}
