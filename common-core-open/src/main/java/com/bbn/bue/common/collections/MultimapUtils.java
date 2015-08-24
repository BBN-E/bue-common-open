package com.bbn.bue.common.collections;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class MultimapUtils {

  private MultimapUtils() {
    throw new UnsupportedOperationException();
  }

  /**
   * This will take a multimap which in fact has a single value map for each key and return a copy
   * of it as an {@link com.google.common.collect.ImmutableMap}. If the same key if mapped to
   * multiple values in the input multimap, an {@link java.lang.IllegalArgumentException} is thrown,
   * so this should generally not be used on multimaps built from user input unless this property is
   * guaranteed to hold.  This is mostly useful for statically building maps which are more
   * naturally represented as inverted multimaps.
   *
   * Preserves iteration order.
   */
  public static <K, V> Map<K, V> copyAsMap(Multimap<K, V> multimap) {
    final Map<K, Collection<V>> inputAsMap = multimap.asMap();
    final ImmutableMap.Builder<K, V> ret = ImmutableMap.builder();

    for (final Map.Entry<K, Collection<V>> mapping : inputAsMap.entrySet()) {
      ret.put(mapping.getKey(), Iterables.getOnlyElement(mapping.getValue()));
    }

    return ret.build();
  }

  /**
   * Converts a {@link com.google.common.collect.Multimap} to a {@link java.util.Map} by selecting a
   * single value for each key and discarding the others, with the selection procedure being defined
   * by {@code reducerFunction}, which may not return {@code null}. Additionally, {@code multimap}
   * may not have {@code null} keys.
   */
  public static <K, V> ImmutableMap<K, V> reduceToMap(Multimap<K, V> multimap,
      Function<? super Collection<V>, ? extends V> reducerFunction) {
    final ImmutableMap.Builder<K, V> ret = ImmutableMap.builder();

    for (final Map.Entry<K, Collection<V>> entry : multimap.asMap().entrySet()) {
      ret.put(entry.getKey(), reducerFunction.apply(entry.getValue()));
    }

    return ret.build();
  }

  /**
   * Performs a (non-strict) composition of two multimaps to an {@link ImmutableSetMultimap}. This
   * returns a new {@link ImmutableSetMultimap} which will contain an entry {@code (k,v)} if and
   * only if there is some {@code i} such that {@code (k, i)} is in {@code first} and {@code (i,v)}
   * is in {@code second}.  Neither {@code first} nor {@code second} is permitted to contain null
   * keys or values. The output of this method is not a view and will not be updated for changes to
   * the input multimaps.
   *
   * This method will allow {@code first} to contain values not found as keys of {@code second}. If
   * you wish to disallow this, see {@link #composeToSetMultimapStrictly(Multimap, Multimap)}.
   */
  public static <K1, K2, V1 extends K2, V2> ImmutableSetMultimap<K1, V2> composeToSetMultimap(
      final Multimap<K1, V1> first, final Multimap<K2, V2> second) {
    final ImmutableSetMultimap.Builder<K1, V2> result = ImmutableSetMultimap.builder();
    for (K1 k1 : first.keySet()) {
      checkNotNull(k1);
      for (V1 v1 : first.get(k1)) {
        checkNotNull(v1);
        result.putAll(k1, second.get(v1));
      }
    }
    return result.build();
  }

  /**
   * Performs a (strict) composition of two multimaps to an {@link ImmutableSetMultimap}. This
   * returns a new {@link ImmutableSetMultimap} which will contain an entry {@code (k,v)} if and
   * only if there is some {@code i} such that {@code (k, i)} is in {@code first} and {@code (i, v)}
   * is in {@code second}.  Neither {@code first} nor {@code second} is permitted to contain null
   * keys or values. The output of this method is not a view and will not be updated for changes to
   * the input multimaps. Strict compositions require that for each entry {@code (k,i)} in {@code
   * first}, there exists an entry {@code (i,v)} in {@code second}.
   */
  public static <K1, K2, V1 extends K2, V2> ImmutableSetMultimap<K1, V2> composeToSetMultimapStrictly(
      final Multimap<K1, V1> first, final Multimap<K2, V2> second) {
    checkArgument(second.keySet().containsAll(first.values()));
    return composeToSetMultimap(first, second);
  }
}
