package com.bbn.bue.common.collections;

import com.bbn.bue.common.StringUtils;
import com.bbn.bue.common.collections.IterableUtils.ZipPair;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.base.Preconditions.checkNotNull;

public final class MapUtils {

  private MapUtils() {
    throw new UnsupportedOperationException();
  }

  /**
   * Pairs up the values of a map by their common keys.
   */
  public static <K, V> PairedMapValues<V> zipValues(final Map<K, V> left, final Map<K, V> right) {
    checkNotNull(left);
    checkNotNull(right);
    final ImmutableList.Builder<ZipPair<V, V>> pairedValues =
        ImmutableList.builder();
    final ImmutableList.Builder<V> leftOnly = ImmutableList.builder();
    final ImmutableList.Builder<V> rightOnly = ImmutableList.builder();

    for (final Map.Entry<K, V> leftEntry : left.entrySet()) {
      final K key = leftEntry.getKey();
      if (right.containsKey(key)) {
        pairedValues.add(ZipPair.from(leftEntry.getValue(), right.get(key)));
      } else {
        leftOnly.add(leftEntry.getValue());
      }
    }

    for (final Map.Entry<K, V> rightEntry : right.entrySet()) {
      if (!left.containsKey(rightEntry.getKey())) {
        rightOnly.add(rightEntry.getValue());
      }
    }

    return new PairedMapValues<V>(pairedValues.build(), leftOnly.build(),
        rightOnly.build());
  }

  /**
   * Return a copy of the input map with keys transformed by {@code keyInjection} and values
   * transformed by {@code valueFunction}. Beware: {@code keyInjection} must be an injection over
   * all the keys of the input map. If two original keys are mapped to the same value, an {@link
   * java.lang.IllegalArgumentException} will be returned.
   *
   * Neither {@code keyInjection} nor {@code valueFunction} may return null. If one does, an
   * exception will be thrown.
   */
  public static <K1, V1, K2, V2> ImmutableMap<K2, V2> copyWithTransformedEntries(Map<K1, V1> input,
      Function<? super K1, K2> keyInjection, Function<? super V1, V2> valueFunction) {
    final ImmutableMap.Builder<K2, V2> ret = ImmutableMap.builder();

    for (final Map.Entry<K1, V1> entry : input.entrySet()) {
      ret.put(keyInjection.apply(entry.getKey()), valueFunction.apply(entry.getValue()));
    }
    return ret.build();
  }

  public static class PairedMapValues<V> {

    public PairedMapValues(final List<ZipPair<V, V>> pairedValues, final List<V> leftOnly,
        final List<V> rightOnly) {
      this.pairedValues = ImmutableList.copyOf(pairedValues);
      this.leftOnly = ImmutableList.copyOf(leftOnly);
      this.rightOnly = ImmutableList.copyOf(rightOnly);
    }

    public List<ZipPair<V, V>> pairedValues() {
      return pairedValues;
    }

    public List<V> leftOnly() {
      return leftOnly;
    }

    public List<V> rightOnly() {
      return rightOnly;
    }

    public boolean perfectlyAligned() {
      return leftOnly.isEmpty() && rightOnly.isEmpty();
    }

    private final List<ZipPair<V, V>> pairedValues;
    private final List<V> leftOnly;
    private final List<V> rightOnly;
  }

  public static <K, V> ImmutableSet<K> allKeys(final Iterable<? extends Map<K, V>> maps) {
    final ImmutableSet.Builder<K> builder = ImmutableSet.builder();

    for (final Map<K, V> map : maps) {
      builder.addAll(map.keySet());
    }

    return builder.build();
  }

  public static <K, V> Function<Map.Entry<K, V>, V> getEntryValue() {
    return new Function<Map.Entry<K, V>, V>() {
      @Override
      public V apply(final Map.Entry<K, V> entry) {
        return entry.getValue();
      }
    };
  }

  public static <K, V> Function<Map.Entry<K, V>, K> getEntryKey() {
    return new Function<Map.Entry<K, V>, K>() {
      @Override
      public K apply(final Map.Entry<K, V> entry) {
        return entry.getKey();
      }
    };
  }

  public static <K, V> Function<Map<K, V>, Iterable<V>> getValues() {
    return new Function<Map<K, V>, Iterable<V>>() {
      @Override
      public Iterable<V> apply(final Map<K, V> input) {
        return input.values();
      }
    };
  }

  public static <K, V extends Comparable<V>> Ordering<Map.Entry<K, V>> byValueOrderingAscending() {
    return Ordering.<V>natural().onResultOf(MapUtils.<K, V>getEntryValue());
  }

  public static <K, V extends Comparable<V>> Ordering<Map.Entry<K, V>> byValueOrderingDescending() {
    return Ordering.<V>natural().onResultOf(MapUtils.<K, V>getEntryValue()).reverse();
  }

  public static <K, V> Ordering<Map.Entry<K, V>> byValueOrdering(final Ordering<V> valueOrdering) {
    return valueOrdering.onResultOf(MapUtils.<K, V>getEntryValue());
  }

  public static <K, V> List<Entry<K, V>> sortedCopyOfEntries(final Map<K, V> map,
      final Ordering<Map.Entry<K, V>> ordering) {
    return ordering.sortedCopy(map.entrySet());
  }

  /**
   * Returns an immutable copy of the given map whose iteration order has keys sorted by the key
   * type's natural ordering. No map keys or values may be {@code null}.
   */
  public static <K extends Comparable<K>, V> ImmutableMap<K, V> copyWithSortedKeys(
      final Map<K, V> map) {
    return copyWithKeysSortedBy(map, Ordering.<K>natural());
  }

  /**
   * Returns an immutable copy of the given map whose iteration order has keys sorted by the given
   * {@link Ordering}. No map keys or values may be {@code null}.
   */
  public static <K, V> ImmutableMap<K, V> copyWithKeysSortedBy(final Map<K, V> map,
      final Ordering<? super K> ordering) {
    final ImmutableMap.Builder<K, V> ret = ImmutableMap.builder();
    for (final K key : ordering.sortedCopy(map.keySet())) {
      ret.put(key, map.get(key));
    }
    return ret.build();
  }

  public static <K extends Comparable<K>, V> Ordering<Entry<K, V>> byKeyDescendingOrdering() {
    return Ordering.<K>natural().onResultOf(MapUtils.<K, V>getEntryKey());
  }

  public static <K, V> Ordering<Entry<K, V>> byKeyOrdering(Ordering<K> keyOrdering) {
    return keyOrdering.onResultOf(MapUtils.<K, V>getEntryKey());
  }

  /**
   * Returns a new map which is contains all the mappings in both provided maps. This method will
   * throw an exception if there is any key {@code K} such that {@code K} is a key in both maps and
   * {@code !first.get(K).equals(second.get(K))}.    The iteration order of the resulting map will
   * contain first all entries in the first map in the order provided by {@code first.entrySet()}
   * followed by all non-duplicate entries in the second map in the order provided by {@code
   * second.entrySet()}.  This method is null-intolerant: if either input map contains {@code null},
   * an exception will be thrown.
   */
  public static <K, V> ImmutableMap<K, V> disjointUnion(Map<K, V> first, Map<K, V> second) {
    checkNotNull(first);
    checkNotNull(second);
    final ImmutableMap.Builder<K, V> ret = ImmutableMap.builder();

    // will throw an exception if there is any null mapping
    ret.putAll(first);
    for (final Entry<K, V> secondEntry : second.entrySet()) {
      final V firstForKey = first.get(secondEntry.getKey());
      if (firstForKey == null) {
        // non-duplicate
        // we know the first map doesn't actually map this key to null
        // or ret.putAll(first) above would fail
        ret.put(secondEntry.getKey(), secondEntry.getValue());
      } else if (firstForKey.equals(secondEntry.getValue())) {
        // duplicate. This is okay. Do nothing.
      } else {
        throw new RuntimeException("When attempting a disjoint map union, " +
            String.format("for key %s, first map had %s but second had %s", secondEntry.getKey(),
                firstForKey, secondEntry.getValue()));
      }
    }

    return ret.build();
  }

  /**
   * Creates a copy of the supplied map with its keys transformed by the supplied function, which
   * must be one-to-one on the keys of the original map. If two keys are mapped to the same value by
   * {@code injection}, an {@code IllegalArgumentException} is thrown.
   */
  public static <K1, K2, V> ImmutableMap<K2, V> copyWithKeysTransformedByInjection(
      final Map<K1, V> map, final Function<? super K1, K2> injection) {
    final ImmutableMap.Builder<K2, V> ret = ImmutableMap.builder();
    for (final Map.Entry<K1, V> entry : map.entrySet()) {
      ret.put(injection.apply(entry.getKey()), entry.getValue());
    }
    return ret.build();
  }

  /**
   * Returns the length of the longest key in a map, or 0 if the map is empty. Useful for printing
   * tables, etc. The map may not have any null keys.
   */
  public static <V> int longestKeyLength(Map<String, V> map) {
    if (map.isEmpty()) {
      return 0;
    }

    return Ordering.natural().max(
        FluentIterable.from(map.keySet())
            .transform(StringUtils.ToLength));
  }

  /**
   * Just like {@link com.google.common.base.Functions#forMap(Map, Object)} except it allows a
   * potentially non-constant {@link Function} as the default.
   */
  public static <K, V> Function<K, V> asFunction(Map<K, ? extends V> map,
      Function<K, ? extends V> defaultFunction) {
    return new ForMapWithDefaultFunction<K, V>(map, defaultFunction);
  }

  // indebted to Guava's Functions.forMap()
  private static class ForMapWithDefaultFunction<K, V> implements Function<K, V> {

    private final Map<K, ? extends V> map;
    private final Function<K, ? extends V> defaultFunction;

    private ForMapWithDefaultFunction(
        final Map<K, ? extends V> map, final Function<K, ? extends V> defaultFunction) {
      this.map = checkNotNull(map);
      this.defaultFunction = checkNotNull(defaultFunction);
    }

    @Override
    public V apply(final K input) {
      final V result = map.get(input);
      return (result != null || map.containsKey(result)) ? result : defaultFunction.apply(input);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(map, defaultFunction);
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      final ForMapWithDefaultFunction other = (ForMapWithDefaultFunction) obj;
      return Objects.equal(this.map, other.map)
          && Objects.equal(this.defaultFunction, other.defaultFunction);
    }

    @Override
    public String toString() {
      return "asFunction(" + map + ", default=" + defaultFunction + ")";
    }
  }

  /**
   * Creates a {@link ImmutableMap} by pairing up a sequence of keys and values. The {@code n}-th
   * key is paired to the {@code n}-th value.  Neither null keys nor null values are permitted.  The
   * keys must be unique or an {@link IllegalArgumentException} will be thrown.  If the numberof
   * elements returned by the two {@link Iterable}s differ, an {@link IllegalArgumentException} will
   * be thrown.
   */
  public static <K, V> ImmutableMap<K, V> copyParallelListsToMap(Iterable<K> keys,
      Iterable<V> values) {
    final ImmutableMap.Builder<K, V> ret = ImmutableMap.builder();

    final Iterator<K> keyIt = keys.iterator();
    final Iterator<V> valueIt = values.iterator();

    while (keyIt.hasNext() && valueIt.hasNext()) {
      ret.put(keyIt.next(), valueIt.next());
    }

    if (!keyIt.hasNext() && !valueIt.hasNext()) {
      return ret.build();
    } else {
      if (keyIt.hasNext()) {
        throw new IllegalArgumentException(
            "When pairing keys and values, there were more keys than values");
      } else {
        throw new IllegalArgumentException(
            "When pairing keys and values, there were more values than keys");
      }
    }
  }
}
