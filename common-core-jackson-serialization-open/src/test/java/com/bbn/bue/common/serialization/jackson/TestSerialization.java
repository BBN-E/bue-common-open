package com.bbn.bue.common.serialization.jackson;

import com.bbn.bue.common.evaluation.FMeasureCounts;

import com.google.common.collect.ImmutableMap;

import junit.framework.TestCase;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class TestSerialization extends TestCase {

  private final JacksonSerializer serializer = JacksonSerializer.forNormalJSON();

  @Test
  public void testFMeasureCounts() throws IOException {
    final Map<String, FMeasureCounts> foo = ImmutableMap.of("Hello",
        FMeasureCounts.from(1, 2, 3));
    final File tmp = File.createTempFile("foo", "bar");
    tmp.deleteOnExit();

    assertEquals(foo, JacksonTestUtils.roundTripThroughSerializer(foo, serializer));
  }

}
