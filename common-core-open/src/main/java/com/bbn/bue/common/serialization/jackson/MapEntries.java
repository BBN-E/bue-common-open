package com.bbn.bue.common.serialization.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Allows serialization of {@link Map}s with non-{@link String} keys using Jackson.  By default,
 * Jackson coerces all map keys to be {@code String}s when serializing, breaking round-tripping
 * when there are non-{@code String} keys.
 *
 * This class can be used with Jackson's converter mechanism to work around this problem
 * by supplying a substitute object for serialization which does not trigger the special key
 * behavior (at some cost to performance/memory...). On the problematic map field, add the
 * following annotations (assuming it is an {@link ImmutableMap}:
 * <pre>
 *   {@code
 *   @JsonSerialize(converter=MapEntries.FromMap}
 *   @JsonDeserialize(converter=MapEntries.ToImmutableMap}
 *   private final ImmutableMap<Foo, Bar> myMap();
 *   }
 * </pre>
 *
 * There is also a converter to an {@link ImmutableBiMap}: {@link ToImmutableBiMap}.
 *
 * Please do not use this class for any purpose other than that described above.
 */
public final class MapEntries<K,V> {
  @JsonProperty("d")
  private List<MapEntry<K,V>> entries;

  private MapEntries(@JsonProperty("d") List<MapEntry<K,V>> entries) {
    // no defensive copy for speed. We trust Jackson not to hold on to this reference,
    // which seems to be implicitly promised
    this.entries = entries;
  }

  private static <K,V> MapEntries<K,V> fromMap(Map<K,V> map) {
    final List<MapEntry<K,V>> entries = new ArrayList<>();
    for (final Map.Entry<K, V> e : map.entrySet()) {
      // we do not make any defensive copy here because this is assumed not to be used
      // outside our converters
      entries.add(new MapEntry<>(e.getKey(), e.getValue()));
    }
    return new MapEntries<>(entries);
  }

  // warning below suppressed because we want ImmutableBiMap to throw its exception
  // if any element is null
  @SuppressWarnings("ConstantConditions")
  private ImmutableMap<K,V> toRegularImmutableMap() {
    final ImmutableMap.Builder<K, V>  ret = ImmutableMap.builder();

    for (final MapEntry<K, V> entry : entries) {
      ret.put(entry.key, entry.value);
    }

    return ret.build();
  }

  // warning below suppressed because we want ImmutableBiMap to throw its exception
  // if any element is null
  @SuppressWarnings("ConstantConditions")
  private ImmutableBiMap<K,V> toImmutableBiMap() {
    final ImmutableBiMap.Builder<K,V> ret = ImmutableBiMap.builder();

    for (final MapEntry<K, V> entry : entries) {
      ret.put(entry.key, entry.value);
    }

    return ret.build();
  }

  public static final class MapEntry<K,V> {
    // nullable in case we ever want to support non-Immutable maps
    @JsonProperty("k")
    @Nullable
    private final K key;
    // nullable in case we ever want to support non-Immutable maps
    @JsonProperty("v")
    @Nullable
    private final V value;

    @JsonCreator
    private MapEntry(@JsonProperty("k") K key, @JsonProperty("v") V value) {
      this.key = key;
      this.value = value;
    }
  }

  public static class FromMap extends
      StdConverter<Map<Object, Object>, MapEntries<Object, Object>> {
    @Override
    public MapEntries<Object, Object> convert(final Map<Object, Object> map) {
      return MapEntries.fromMap(map);
    }
  }

  public static class ToImmutableMap extends StdConverter<MapEntries<Object, Object>, ImmutableMap<Object, Object>> {
    @Override
    public ImmutableMap<Object, Object> convert(final MapEntries<Object, Object> proxy) {
      return proxy.toRegularImmutableMap();
    }
  }

  public static class ToImmutableBiMap extends StdConverter<MapEntries<Object, Object>, ImmutableBiMap<Object, Object>> {
    @Override
    public ImmutableBiMap<Object, Object> convert(final MapEntries<Object, Object> proxy) {
      return proxy.toImmutableBiMap();
    }
  }
}


