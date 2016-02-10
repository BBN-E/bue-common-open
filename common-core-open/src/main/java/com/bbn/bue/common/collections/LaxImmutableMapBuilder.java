package com.bbn.bue.common.collections;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

import java.util.Comparator;
import java.util.Map;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Guava's {@link com.google.common.collect.ImmutableMap.Builder} disallows adding the same key
 * twice, even with the same value. In many situations this is inconvenient, so this interface
 * is the parent of builders which relax this constraint in various ways.
 * See
 * {@link MapUtils#immutableMapBuilderAllowingSameEntryTwice()},
 * {@link MapUtils#immutableMapBuilderIgnoringDuplicates()}, and
 * {@link MapUtils#immutableMapBuilderResolvingDuplicatesBy(Comparator)}.
 *
 *
 * You might think that this is unnecessary because you can simply build your map using e.g.
 * a {@link java.util.HashMap} and then call {@link ImmutableMap#copyOf(Iterable)}, but that loses
 * {@link ImmutableMap.Builder}'s very nice determinism guarantees.
 *
 * These builders are generally less efficient than {@link ImmutableMap.Builder}, so
 * be cautious using them in performance-sensitive code.
 */
public interface LaxImmutableMapBuilder<K,V> {
  LaxImmutableMapBuilder<K,V> put(K key, V value);
  LaxImmutableMapBuilder<K,V> putAll(Map<? extends K, ? extends V> map);
  LaxImmutableMapBuilder<K, V> putAll(Iterable<? extends Map.Entry<? extends K, ? extends V>> entries);

  /**
   * See {@link ImmutableMap.Builder#orderEntriesByValue(Comparator)}
   */
  LaxImmutableMapBuilder<K, V> orderEntriesByValue(Comparator<? super V> valueComparator);
  ImmutableMap<K,V> build();
}

/**
 * Implementation for {@link LaxImmutableMapBuilder}s which never need to change their mind.
 */
final class MonotonicLaxImmutableMapBuilder<K,V> implements LaxImmutableMapBuilder<K,V> {

  private final Map<K, V> mappingsSeen = Maps.newHashMap();
  private final ImmutableMap.Builder<K, V> innerBuilder = ImmutableMap.builder();
  private final boolean keepFirst;

  MonotonicLaxImmutableMapBuilder(final boolean keepFirst) {
    this.keepFirst = keepFirst;
  }

  public LaxImmutableMapBuilder<K, V> put(K key, V value) {
    checkNotNull(key);
    checkNotNull(value);
    final V existingMapping = mappingsSeen.get(key);
    if (null == existingMapping) {
      mappingsSeen.put(key, value);
      innerBuilder.put(key, value);
    } else if (existingMapping.equals(value)) {
      // do nothing
    } else if (keepFirst) {
      // do nothing, we have been requested to silently keep the first value
    } else {
      throw new IllegalArgumentException("Refusing to add two mappings for the key "
          + key + ": " + existingMapping + " and " + value);
    }

    return this;
  }

  public LaxImmutableMapBuilder<K,V> putAll(Map<? extends K, ? extends V> map) {
    for (final Map.Entry<? extends K, ? extends V> e : map.entrySet()) {
      put(e.getKey(), e.getValue());
    }
    return this;
  }

  public LaxImmutableMapBuilder<K, V> putAll(Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
    for (final Map.Entry<? extends K, ? extends V> e : entries) {
      put(e.getKey(), e.getValue());
    }
    return this;
  }

  /**
   * See {@link ImmutableMap.Builder#orderEntriesByValue(Comparator)}
   */
  public LaxImmutableMapBuilder<K, V> orderEntriesByValue(Comparator<? super V> valueComparator) {
    innerBuilder.orderEntriesByValue(valueComparator);
    return this;
  }

  public ImmutableMap<K,V> build() {
    return innerBuilder.build();
  }
}

/**
 * Implementation for {@link LaxImmutableMapBuilder}s which do need to change their mind.
 * @param <K>
 * @param <V>
 */
final class NonMonotonicLaxImmutableMapBuilder<K,V> implements LaxImmutableMapBuilder<K,V> {
  private final ImmutableSet.Builder<K> orderKeysSeenIn = ImmutableSet.builder();
  private final Map<K,V> mappingsSeen = Maps.newHashMap();
  private final Ordering<? super V> conflictComparator;
  private @Nullable Comparator<? super V> immutableMapEntryOrdering = null;

  NonMonotonicLaxImmutableMapBuilder(final Ordering<? super V> conflictComparator) {
    this.conflictComparator = checkNotNull(conflictComparator);
  }

  public LaxImmutableMapBuilder<K,V> put(K key, V value) {
    checkNotNull(key);
    checkNotNull(value);
    final V existingMapping = mappingsSeen.get(key);
    if (null == existingMapping) {
      mappingsSeen.put(key, value);
      orderKeysSeenIn.add(key);
    } else if (existingMapping.equals(value)) {
      // do nothing
    } else {
      // add only if new value is larger by the provided comparator
      if (conflictComparator.max(existingMapping, value).equals(value)) {
        mappingsSeen.put(key, value);
      }
    }
    return this;
  }

  public LaxImmutableMapBuilder<K,V> putAll(Map<? extends K, ? extends V> map) {
    for (final Map.Entry<? extends K, ? extends V> e : map.entrySet()) {
      put(e.getKey(), e.getValue());
    }
    return this;
  }

  public LaxImmutableMapBuilder<K, V> putAll(Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
    for (final Map.Entry<? extends K, ? extends V> e : entries) {
      put(e.getKey(), e.getValue());
    }
    return this;
  }

  /**
   * See {@link ImmutableMap.Builder#orderEntriesByValue(Comparator)}
   */
  public LaxImmutableMapBuilder<K, V> orderEntriesByValue(Comparator<? super V> valueComparator) {
    this.immutableMapEntryOrdering = checkNotNull(valueComparator);
    return this;
  }

  public ImmutableMap<K,V> build() {
    final ImmutableMap.Builder<K, V> ret = ImmutableMap.builder();
    if (immutableMapEntryOrdering != null) {
      ret.orderEntriesByValue(immutableMapEntryOrdering);
    }

    for (final K key : orderKeysSeenIn.build()) {
      ret.put(key, mappingsSeen.get(key));
    }

    return ret.build();
  }
}
