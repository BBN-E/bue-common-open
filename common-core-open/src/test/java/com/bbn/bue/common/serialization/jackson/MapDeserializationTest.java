package com.bbn.bue.common.serialization.jackson;

import com.bbn.bue.common.TextGroupImmutable;
import com.bbn.bue.common.io.ByteArraySink;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.io.ByteSource;

import org.immutables.value.Value;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link MapEntries} and {@link MultimapEntries}. See those for context.
 */
public class MapDeserializationTest {
  // we want to check we can round-trip serialize a complex object involving maps and mulitmaps
  // with non-string keys

  final MapSerializationTestObject monoMorphicObj = ImmutableMapSerializationTestObject.builder()
      .putMap(ImmutableMapSerializationTestKey.builder().foo("blah").bar("meep").build(),
          ImmutableMapSerializationTestValue.builder().hello("olleh").world("dlrow").build())
      .putMap(ImmutableMapSerializationTestKey.builder().foo("halb").bar("peem").build(),
          ImmutableMapSerializationTestValue.builder().hello("355").world("3545").build()).build();

  // we want to do this both with and without polymorphic keys and values because Jackson may
  // add additional type information in polymorphic cases
  final MapSerializationPolymorphicTestObject objWithPolymorphic = ImmutableMapSerializationPolymorphicTestObject.builder()
      .putMap(ImmutableMapSerializationTestKey2.builder().oof("blah").rab("meep").build(),
          ImmutableMapSerializationTestValue.builder().hello("olleh").world("dlrow").build())
      .putMap(ImmutableMapSerializationTestKey.builder().foo("halb").bar("peem").build(),
          ImmutableMapSerializationTestValue2.builder().olleh("355").dlrow("3545").build())
      .putBimap(ImmutableMapSerializationTestKey2.builder().oof("blah").rab("meep").build(),
          ImmutableMapSerializationTestValue.builder().hello("olleh").world("dlrow").build())
      .putBimap(ImmutableMapSerializationTestKey.builder().foo("halb").bar("peem").build(),
          ImmutableMapSerializationTestValue2.builder().olleh("355").dlrow("3545").build())
      .putListMultimap(ImmutableMapSerializationTestKey.builder().foo("halb").bar("peem").build(),
          ImmutableMapSerializationTestValue2.builder().olleh("355").dlrow("3545").build())
      .putListMultimap(ImmutableMapSerializationTestKey.builder().foo("halb").bar("peem").build(),
          ImmutableMapSerializationTestValue2.builder().olleh("serffg").dlrow("sdf").build())
      .putSetMultimap(ImmutableMapSerializationTestKey.builder().foo("halb").bar("peem").build(),
          ImmutableMapSerializationTestValue2.builder().olleh("meep").dlrow("meep").build())
      .putSetMultimap(ImmutableMapSerializationTestKey.builder().foo("halb").bar("peem").build(),
          ImmutableMapSerializationTestValue2.builder().olleh("lala").dlrow("lala").build())
      .putSetMultimap(ImmutableMapSerializationTestKey.builder().foo("blah").bar("peem").build(),
          ImmutableMapSerializationTestValue2.builder().olleh("lala").dlrow("lala").build())
      .build();

  @Test
  public void testMonomorphicMapSerialization() throws IOException {
    final JacksonSerializer newSerializer = JacksonSerializer.builder().prettyOutput()
        .forJson().build();
    final ByteArraySink byteSink = ByteArraySink.create();
    newSerializer.serializeTo(monoMorphicObj, byteSink);
    final Object roundTripped =
        newSerializer.deserializeFrom(ByteSource.wrap(byteSink.toByteArray()));
    assertEquals(monoMorphicObj, roundTripped);
  }

  @Test
  public void testMapSerializationPolymorphic() throws IOException {

    final JacksonSerializer newSerializer = JacksonSerializer.builder().prettyOutput()
        .forJson().build();

    final ByteArraySink byteSink = ByteArraySink.create();

    newSerializer.serializeTo(objWithPolymorphic, byteSink);

    final Object roundTripped =
        newSerializer.deserializeFrom(ByteSource.wrap(byteSink.toByteArray()));
    assertEquals(objWithPolymorphic, roundTripped);
  }
}

interface MapSerializationPolymorphicTestKey {

}

interface MapSerializationPolymorphicTestValue {

}

@TextGroupImmutable
@Value.Immutable
@JsonSerialize(as=ImmutableMapSerializationTestKey.class)
@JsonDeserialize(as=ImmutableMapSerializationTestKey.class)
abstract class MapSerializationTestKey implements MapSerializationPolymorphicTestKey {
  abstract String foo();
  abstract String bar();
}

@TextGroupImmutable
@Value.Immutable
@JsonSerialize(as=ImmutableMapSerializationTestKey2.class)
@JsonDeserialize(as=ImmutableMapSerializationTestKey2.class)
abstract class MapSerializationTestKey2 implements MapSerializationPolymorphicTestKey {
  abstract String oof();
  abstract String rab();
}

@TextGroupImmutable
@Value.Immutable
@JsonSerialize(as=ImmutableMapSerializationTestValue.class)
@JsonDeserialize(as=ImmutableMapSerializationTestValue.class)
abstract class MapSerializationTestValue implements MapSerializationPolymorphicTestValue {
  abstract String hello();
  abstract String world();
}

@TextGroupImmutable
@Value.Immutable
@JsonSerialize(as=ImmutableMapSerializationTestValue2.class)
@JsonDeserialize(as=ImmutableMapSerializationTestValue2.class)
abstract class MapSerializationTestValue2 implements MapSerializationPolymorphicTestValue {
  abstract String olleh();
  abstract String dlrow();
}

@TextGroupImmutable
@Value.Immutable
@JsonSerialize(as=ImmutableMapSerializationTestObject.class)
@JsonDeserialize(as=ImmutableMapSerializationTestObject.class)
abstract class MapSerializationTestObject {
  static final Map<String, String> foo = new HashMap<>();
  @JsonSerialize(converter = MapEntries.FromMap.class)
  @JsonDeserialize(converter = MapEntries.ToImmutableMap.class)
  abstract ImmutableMap<MapSerializationTestKey, MapSerializationTestValue> map();
}

@TextGroupImmutable
@Value.Immutable
@JsonSerialize(as=ImmutableMapSerializationPolymorphicTestObject.class)
@JsonDeserialize(as=ImmutableMapSerializationPolymorphicTestObject.class)
abstract class MapSerializationPolymorphicTestObject {
  @JsonSerialize(converter = MapEntries.FromMap.class)
  @JsonDeserialize(converter = MapEntries.ToImmutableMap.class)
  abstract ImmutableMap<MapSerializationPolymorphicTestKey, MapSerializationPolymorphicTestValue> map();

  @JsonSerialize(converter = MapEntries.FromMap.class)
  @JsonDeserialize(converter = MapEntries.ToImmutableBiMap.class)
  abstract ImmutableBiMap<MapSerializationPolymorphicTestKey, MapSerializationPolymorphicTestValue> bimap();

  @JsonSerialize(converter = MultimapEntries.FromMultimap.class)
  @JsonDeserialize(converter = MultimapEntries.ToImmutableListMultimap.class)
  abstract ImmutableListMultimap<MapSerializationPolymorphicTestKey, MapSerializationPolymorphicTestValue> listMultimap();

  @JsonSerialize(converter = MultimapEntries.FromMultimap.class)
  @JsonDeserialize(converter = MultimapEntries.ToImmutableSetMultimap.class)
  abstract ImmutableSetMultimap<MapSerializationPolymorphicTestKey, MapSerializationPolymorphicTestValue> setMultimap();
}


