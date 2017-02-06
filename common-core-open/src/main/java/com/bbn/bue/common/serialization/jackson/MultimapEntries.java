package com.bbn.bue.common.serialization.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Allows serialization of {@link Multimap}s with non-{@link String} keys using Jackson.  By
 * default, Jackson coerces all multimap keys to be {@code String}s when serializing, breaking
 * round-tripping when there are non-{@code String} keys.
 *
 * This class can be used with Jackson's converter mechanism to work around this problem
 * by supplying a substitute object for serialization which does not trigger the special key
 * behavior (at some cost to performance/memory...). On the problematic multimap field, add the
 * following annotations (assuming it is an {@link ImmutableListMultimap}:
 * <pre>
 *   {@code
 *   @JsonSerialize(converter=MultimapEntries.FromMultimap}
 *   @JsonDeserialize(converter=MapEntries.ToImmutableListMultimap}
 *   private final ImmutableListMap<Foo, Bar> myMultimap();
 *   }
 * </pre>
 *
 * There is also a converter to an {@link ImmutableSetMultimap}: {@link ToImmutableSetMultimap}.
 *
 * Please do not use this class for any purpose other than that described above.
 */
public final class MultimapEntries<K,V> {
  @JsonProperty("d")
  private List<MultimapEntry<K,V>> entries;

  private MultimapEntries(@JsonProperty("d") List<MultimapEntry<K,V>> entries) {
    // we do not make a defensive copy for speed and because Jackson seems to implicitly promise
    // it will not hold onto the reference
    this.entries = entries;
  }

  static <K,V> MultimapEntries<K,V> fromMap(Multimap<K,V> multimap) {
    final List<MultimapEntry<K,V>> entries = new ArrayList<>();
    for (final Map.Entry<K, Collection<V>> e: multimap.asMap().entrySet()) {
      // we do not defensively copy the key or the value since this should only be used in
      // Jackson converters, where it is not necessary
      entries.add(new MultimapEntry<>(e.getKey(), e.getValue()));
    }
    return new MultimapEntries<>(entries);
  }

  // exception on null desirable
  @SuppressWarnings("ConstantConditions")
  ImmutableListMultimap<K,V> toImmutableListMultimap() {
    final ImmutableListMultimap.Builder<K, V>  ret = ImmutableListMultimap.builder();

    for (final MultimapEntry<K, V> entry : entries) {
      ret.putAll(entry.key, entry.values);
    }

    return ret.build();
  }

  // exception on null desirable
  @SuppressWarnings("ConstantConditions")
  ImmutableSetMultimap<K,V> toImmutableSetMultimap() {
    final ImmutableSetMultimap.Builder<K, V>  ret = ImmutableSetMultimap.builder();

    for (final MultimapEntry<K, V> entry : entries) {
      ret.putAll(entry.key, entry.values);
    }

    return ret.build();
  }

  public static final class MultimapEntry<K,V> {
    @JsonProperty("k")
    @Nullable
    private final K key;
    @JsonProperty("v")
    private final Collection<V> values;

    @JsonCreator
    private MultimapEntry(@JsonProperty("k") K key, @JsonProperty("v") Collection<V> values) {
      this.key = key;
      this.values = checkNotNull(values);
    }
  }

  public static class FromMultimap extends
      StdConverter<Multimap<Object, Object>, MultimapEntries<Object, Object>> {
    @Override
    public MultimapEntries<Object, Object> convert(final Multimap<Object, Object> multimap) {
      return MultimapEntries.fromMap(multimap);
    }
  }

  public static class ToImmutableListMultimap
      extends StdConverter<MultimapEntries<Object, Object>, ImmutableListMultimap<Object, Object>> {
    @Override
    public ImmutableListMultimap<Object, Object> convert(final MultimapEntries<Object, Object> proxy) {
      return proxy.toImmutableListMultimap();
    }
  }

  public static class ToImmutableSetMultimap
      extends StdConverter<MultimapEntries<Object, Object>, ImmutableSetMultimap<Object, Object>> {
    @Override
    public ImmutableSetMultimap<Object, Object> convert(final MultimapEntries<Object, Object> proxy) {
      return proxy.toImmutableSetMultimap();
    }
  }
}
