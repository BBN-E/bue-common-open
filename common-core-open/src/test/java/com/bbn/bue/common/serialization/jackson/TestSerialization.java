package com.bbn.bue.common.serialization.jackson;

import com.bbn.bue.common.evaluation.FMeasureCounts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertEquals;

public class TestSerialization {

  private final JacksonSerializer serializer = JacksonSerializer.builder().prettyOutput().build();

  // can't be type-safe when deserializing
  @SuppressWarnings("unchecked")
  @Test
  public void testFMeasureCounts() throws IOException {
    final Map<String, FMeasureCounts> foo = ImmutableMap.of("Hello",
        FMeasureCounts.fromTPFPFN(1, 2, 3));
    final File tmp = File.createTempFile("foo", "bar");
    tmp.deleteOnExit();

    assertEquals(foo, JacksonTestUtils.roundTripThroughSerializer(foo, serializer));
  }

  // can't be type-safe when deserializing
  @SuppressWarnings("unchecked")
  @Test
  public void testSerializingFromString() throws IOException {
    final Map<String, FMeasureCounts> foo = ImmutableMap.of("Hello",
        FMeasureCounts.fromTPFPFN(1, 2, 3));
    final String serialized = serializer.writeValueAsString(foo);
    assertEquals(foo, serializer.deserializeFromString(serialized, foo.getClass()));
  }

  // can't be type-safe when deserializing
  @SuppressWarnings("unchecked")
  @Test
  public void testImmutableMapProxy() throws IOException {
    final ImmutableMapWrapper expected = new ImmutableMapWrapper(ImmutableMap.of("a", 1));
    final String serialized = serializer.writeValueAsString(expected);
    assertEquals(expected, serializer.deserializeFromString(serialized, expected.getClass()));
  }

  // can't be type-safe when deserializing
  // suppress EqualsHashCode because we only care about equality for the test
  @SuppressWarnings({"unchecked", "EqualsHashCode"})
  @Test
  public void testImmutableMultimapProxy() throws IOException {
    final ImmutableMultimapWrapper expected = new ImmutableMultimapWrapper(
        ImmutableMultimap.of("a", 1, "a", 2, "b", 3));
    final String serialized = serializer.writeValueAsString(expected);
    assertEquals(expected, serializer.deserializeFromString(serialized, expected.getClass()));
  }

  private static class ImmutableMapWrapper {
    private final ImmutableMap map;

    private ImmutableMapWrapper(ImmutableMap map) {
      this.map = checkNotNull(map);
    }

    @JsonCreator
    private static ImmutableMapWrapper fromJson(@JsonProperty("map") ImmutableMapProxy map) {
      return new ImmutableMapWrapper(map.toImmutableMap());
    }

    @JsonProperty("map")
    private ImmutableMapProxy mapProxy() {
      return ImmutableMapProxy.forMap(map);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      ImmutableMapWrapper that = (ImmutableMapWrapper) o;
      return Objects.equal(map, that.map);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(map).toString();
    }
  }

  private static class ImmutableMultimapWrapper {
    private final ImmutableMultimap map;

    private ImmutableMultimapWrapper(ImmutableMultimap map) {
      this.map = checkNotNull(map);
    }

    @SuppressWarnings("deprecation")
    @JsonCreator
    private static ImmutableMultimapWrapper fromJson(@JsonProperty("map") ImmutableMultimapProxy map) {
      return new ImmutableMultimapWrapper(map.toImmutableMultimap());
    }

    @SuppressWarnings("deprecation")
    @JsonProperty("map")
    private ImmutableMultimapProxy mapProxy() {
      return ImmutableMultimapProxy.forMultimap(map);
    }

    // warning suppressed because we only care about equality for the test
    @SuppressWarnings("EqualsHashCode")
    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      ImmutableMultimapWrapper that = (ImmutableMultimapWrapper) o;
      return Objects.equal(map, that.map);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(map).toString();
    }
  }
}
