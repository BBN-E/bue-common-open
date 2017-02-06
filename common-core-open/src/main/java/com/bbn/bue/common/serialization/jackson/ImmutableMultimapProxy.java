package com.bbn.bue.common.serialization.jackson;

import com.bbn.bue.common.collections.MapUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Works around a Jackson issue in serializing multimaps in a similar fashion to {@link ImmutableMapProxy}.
 * To use this:
 * <ol>
 *   <li>Don't make the multimap itself a {@code JsonProperty}</li>
 *   <li>Provide a {@code JsonProperty}-annotated package-private accessor which returns
 *   {@code ImmutableMultimapProxy.forMultimap(myMap)}</li>
 *   <li>In your {@code JsonCreator} method, take an {@code ImmutableMultimapProxy} as an argument and call
 *   {@link #toImmutableMultimap()}</li>
 * </ol>
 *
 * @deprecated Prefer {@link MapEntries}
 */
@Deprecated
public class ImmutableMultimapProxy<K, V>  {

  @JsonProperty("keys")
  private final List<K> keys;
  @JsonProperty("values")
  private final List<Collection<V>> values;

  private ImmutableMultimapProxy(
      @JsonProperty("keys") final List<K> keys,
      @JsonProperty("values") final List<Collection<V>> values) {
    this.keys = checkNotNull(keys);
    this.values = checkNotNull(values);
    checkArgument(keys.size() == values.size());
  }

  @Deprecated
  @SuppressWarnings("deprecation")
  public static <K, V> ImmutableMultimapProxy<K, V> forMultimap(Multimap<K, V> map) {
    final List<K> keys = Lists.newArrayListWithCapacity(map.size());
    final List<Collection<V>> values = Lists.newArrayListWithCapacity(map.size());
    for (final Map.Entry<K, Collection<V>> e : map.asMap().entrySet()) {
      keys.add(e.getKey());
      values.add(e.getValue());
    }
    return new ImmutableMultimapProxy<>(keys, values);
  }

  public ImmutableMultimap<K, V> toImmutableMultimap() {
    final ImmutableMap<K, Collection<V>> map = MapUtils.copyParallelListsToMap(keys, values);
    final ImmutableMultimap.Builder<K, V> ret = ImmutableMultimap.builder();
    for (final Map.Entry<K, Collection<V>> entry: map.entrySet()) {
      ret.putAll(entry.getKey(), entry.getValue());
    }
    return ret.build();
  }
}
