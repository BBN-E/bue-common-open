package com.bbn.bue.common.serialization.jackson;

import com.bbn.bue.common.collections.MapUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Works around a Jackson issue in serializing maps. Jackson, frustratingly, serializes maps to JSON
 * by turning the keys into Strings, which typically don't want.  To get around this: <ol> <li>Don't
 * make the map itself a {@code JsonProperty}</li> <li>Provide a {@code JsonProperty}-annotated
 * package-private accessor which returns {@code ImmutableMapProxy.forMap(myMap)}</li> <li>In your
 * {@code JsonCreator} method, take an {@code ImmutableMapProxy} as an argument and call {@link
 * #toImmutableMap()}</li> </ol>
 *
 * @deprecated Prefer {@link MapEntries}
 */
@Deprecated
public final class ImmutableMapProxy<K, V> {

  @JsonProperty("keys")
  private final List<K> keys;
  @JsonProperty("values")
  private final List<V> values;

  private ImmutableMapProxy(@JsonProperty("keys") final List<K> keys,
      @JsonProperty("values") final List<V> values) {
    this.keys = checkNotNull(keys);
    this.values = checkNotNull(values);
    checkArgument(keys.size() == values.size());
  }

  @Deprecated
  @SuppressWarnings("deprecation")
  public static <K, V> ImmutableMapProxy<K, V> forMap(Map<K, V> map) {
    final List<K> keys = Lists.newArrayListWithCapacity(map.size());
    final List<V> values = Lists.newArrayListWithCapacity(map.size());
    for (final Map.Entry<K, V> e : map.entrySet()) {
      keys.add(e.getKey());
      values.add(e.getValue());
    }
    return new ImmutableMapProxy<K, V>(keys, values);
  }

  public ImmutableMap<K, V> toImmutableMap() {
    return MapUtils.copyParallelListsToMap(keys, values);
  }
}

