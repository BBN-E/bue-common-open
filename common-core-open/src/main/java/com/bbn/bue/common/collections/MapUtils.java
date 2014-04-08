package com.bbn.bue.common.collections;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.bbn.bue.common.collections.IterableUtils.ZipPair;
import com.google.common.base.Function;
import com.google.common.collect.*;

import static com.google.common.base.Preconditions.checkNotNull;

public final class MapUtils {
	private MapUtils() { throw new UnsupportedOperationException(); }

	/**
	 * Pairs up the values of a map by their common keys.
	 * @param left
	 * @param right
	 * @return
	 */
	public static <K,V> PairedMapValues<V> zipValues(final Map<K,V> left, final Map<K,V> right) {
		checkNotNull(left);
		checkNotNull(right);
		final ImmutableList.Builder<ZipPair<V,V>> pairedValues =
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

	public static class PairedMapValues<V> {
		public PairedMapValues(final List<ZipPair<V,V>> pairedValues, final List<V> leftOnly,
			final List<V> rightOnly)
		{
			this.pairedValues = ImmutableList.copyOf(pairedValues);
			this.leftOnly = ImmutableList.copyOf(leftOnly);
			this.rightOnly = ImmutableList.copyOf(rightOnly);
		}

		public List<ZipPair<V,V>> pairedValues() { return pairedValues; }
		public List<V> leftOnly() { return leftOnly; }
		public List<V> rightOnly() { return rightOnly; }
		public boolean perfectlyAligned() {
			return leftOnly.isEmpty() && rightOnly.isEmpty();
		}

		private final List<ZipPair<V,V>> pairedValues;
		private final List<V> leftOnly;
		private final List<V> rightOnly;
	}

	public static <K,V> ImmutableSet<K> allKeys(final Iterable<? extends Map<K, V>> maps) {
		final ImmutableSet.Builder<K> builder = ImmutableSet.builder();

		for (final Map<K,V> map : maps) {
			builder.addAll(map.keySet());
		}

		return builder.build();
	}

	public static <K,V> Function<Map.Entry<K, V>,V> getEntryValue() {
		return new Function<Map.Entry<K,V>, V>() {
			@Override
			public V apply(final Map.Entry<K,V> entry) {
				return entry.getValue();
			}
		};
	}

	public static <K,V> Function<Map.Entry<K, V>,K> getEntryKey() {
		return new Function<Map.Entry<K,V>, K>() {
			@Override
			public K apply(final Map.Entry<K,V> entry) {
				return entry.getKey();
			}
		};
	}

	public static <K,V extends Comparable<V>> Ordering<Map.Entry<K,V>> byValueOrderingAscending() {
		return Ordering.<V>natural().onResultOf(MapUtils.<K,V>getEntryValue());
	}

	public static <K,V extends Comparable<V>> Ordering<Map.Entry<K,V>> byValueOrderingDescending() {
		return Ordering.<V>natural().onResultOf(MapUtils.<K,V>getEntryValue()).reverse();
	}

	public static <K,V> Ordering<Map.Entry<K,V>> byValueOrdering(final Ordering<V> valueOrdering) {
		return valueOrdering.onResultOf(MapUtils.<K,V>getEntryValue());
	}

	public static <K,V> List<Entry<K, V>> sortedCopyOfEntries(final Map<K,V> map, final Ordering<Map.Entry<K, V>> ordering) {
		return ordering.sortedCopy(map.entrySet());
	}

	public static <K extends Comparable<K>,V> Ordering<Entry<K, V>> byKeyDescendingOrdering() {
		return Ordering.<K>natural().onResultOf(MapUtils.<K,V>getEntryKey());
	}

    /**
     * Returns a new map which is contains all the mappings in both provided maps. This
     * method will throw an exception if there is any key {@code K} such that {@code K} is a key in both
     * maps and {@code !first.get(K).equals(second.get(K))}.    The iteration order of the resulting map
     * will contain first all entries in the first map in the order provided by {@code first.entrySet()}
     * followed by all non-duplicate entries in the second map in the order provided by
     * {@code second.entrySet()}.  This method is null-intolerant: if either input map contains {@code null},
     * an exception will be thrown.
     *
     * @param first
     * @param second
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K,V> ImmutableMap<K,V> disjointUnion(Map<K,V> first, Map<K,V> second) {
        checkNotNull(first);
        checkNotNull(second);
        final ImmutableMap.Builder<K,V> ret = ImmutableMap.builder();

        // will throw an exception if there is any null mapping
        ret.putAll(first);
        for (final Entry<K,V> secondEntry : second.entrySet()) {
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
	 * must be one-to-one on the keys of the original map. If two keys are mapped to the same value
	 * by {@code injection}, an {@code IllegalArgumentException} is thrown.
	 *
	 * @param map
	 * @param injection
	 * @return
	 */
	public static <K1, K2, V> ImmutableMap<K2, V> copyWithKeysTransformedByInjection(
		final Map<K1, V> map, final Function<? super K1, K2> injection)
	{
		final ImmutableMap.Builder<K2, V> ret = ImmutableMap.builder();
		for (final Map.Entry<K1, V> entry : map.entrySet()) {
			ret.put(injection.apply(entry.getKey()), entry.getValue());
		}
		return ret.build();
	}
}
