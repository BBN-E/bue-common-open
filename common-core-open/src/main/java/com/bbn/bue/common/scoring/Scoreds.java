package com.bbn.bue.common.scoring;

import com.bbn.bue.common.primitives.DoubleUtils;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

import java.util.Map;

public final class Scoreds {
	private Scoreds() { throw new UnsupportedOperationException(); }

    /**
     * Returns a function which maps a scored item to the item itself.
     * @return
     */
    public static <X> Function<Scored<X>, X> itemsOnly() {
    	return new ItemsOnly<X>();
    }

    /**
     * Returns a function which maps a scored item to its score.
     * @return
     */
    public static <X> Function<Scored<X>, Double> scoresOnly() {
    	return new ScoresOnly<X>();
    }

    private static class ItemsOnly<T> implements Function<Scored<T>,T> {
    	@Override
		public T apply(final Scored<T> x) {
    		return x.item();
    	}
    }

    private static class ScoresOnly<T> implements Function<Scored<T>, Double> {
    	@Override
		public  Double apply(final Scored<T> x) {
    		return x.score();
    	}
    }

	/**
	 * Easy way to pass a {@link Predicate} through to the item of a <code>Scored<T></code>.
	 * @param pred The <code>Predicate</code> to apply to the item
	 * @return A <code>Predicate</code> on <code>Scored<X>s</code> which applies the supplied predicate to the item.
	 */
	public static <T> Predicate<Scored<T>> ItemIs(final Predicate<T> pred) {
		return new Predicate<Scored<T>>() {
			@Override
			public boolean apply(final Scored<T> x) {
				return pred.apply(x.item());
			}
		};
	}


	/**
	 * Easy way to pass a {@link Predicate} through to the score of a <code>Scored<T></code>.
	 * @param pred The <code>Predicate</code> to apply to the score
	 * @return A <code>Predicate</code> on <code>Scored<X>s</code> which applies the supplied predicate to the score.
	 */
	public static <X> Predicate<Scored<X>> scoreIs(final Predicate<Double> pred) {
		return new Predicate<Scored<X>>() {
			@Override
			public boolean apply(final Scored<X> x) {
				return pred.apply(x.score());
			}
		};
	}

	public static final <X> Predicate<Scored<X>> ScoreIsFinite() {
		return Scoreds.<X>scoreIs(DoubleUtils.IsFinite);
	}

	public static <T>  Function<Map.Entry<T, Integer>, Scored<T>> mapEntryToIntegerToScored() {
		return new MapEntryToIntegerToScored<T>();
	}

	private static class MapEntryToIntegerToScored<T> implements Function<Map.Entry<T,Integer>, Scored<T>> {
		@Override
		public Scored<T> apply(final Map.Entry<T, Integer> entry) {
			return new Scored<T>(entry.getKey(), entry.getValue());
		}
	}

	public static <T>  Function<Map.Entry<T, Double>, Scored<T>> mapEntryToDoubleToScored() {
		return new MapEntryToDoubleToScored<T>();
	}

	private static class MapEntryToDoubleToScored<T> implements Function<Map.Entry<T,Double>, Scored<T>> {
		@Override
		public Scored<T> apply(final Map.Entry<T, Double> entry) {
			return new Scored<T>(entry.getKey(), entry.getValue());
		}
	}

    /**
     * Comparator which compares Scoreds first by score, then by item, where the item
     * ordering to use is explicitly specified.
     * @return
     */
    public static final <T extends Comparable<T>> Ordering<Scored<T>> ByScoreThenByItem(
            Ordering<T> itemOrdering)
    {
        final Ordering<Scored<T>> byItem = itemOrdering.onResultOf(Scoreds.<T>itemsOnly());
        final Ordering<Scored<T>> byScore = Scoreds.<T>ByScoreOnly();

        return Ordering.compound(ImmutableList.of(byScore, byItem));
    }

	/**
	 * Comparator which compares Scoreds first by score, then by item.
	 * @return
	 */
	public static final <T extends Comparable<T>> Ordering<Scored<T>> ByScoreThenByItem() {
        final Ordering<Scored<T>> byItem = Scoreds.<T>ByItemOnly();
        final Ordering<Scored<T>> byScore = Scoreds.<T>ByScoreOnly();

        return Ordering.compound(ImmutableList.of(byScore, byItem));
	}

    /**
     * Ordering of {@code Scored}s by the natural ordering on items.
     */
    public static final <T extends Comparable<T>> Ordering<Scored<T>> ByItemOnly() {
        return Ordering.natural().onResultOf(Scoreds.<T>itemsOnly());
    }

    /**
     * Ordering of {@code Scored}s using the supplied ordering on items.
     * @param itemOrdering
     * @param <T>
     * @return
     */
    public static final <T extends Comparable<T>> Ordering<Scored<T>>
        ByItemOnly(Ordering<T> itemOrdering)
    {
        return itemOrdering.onResultOf(Scoreds.<T>itemsOnly());
    }

	/**
	 * Comparator which compares by scores only. Beware: using this comparator in things
	 * which depend on the comparator for equality judgements (e.g. SortedSet) may result
	 * in things with the same score being considered equal.
	 * @return
	 */
	public static final <T> Ordering<Scored<T>> ByScoreOnly() {
        return Ordering.natural().onResultOf(Scoreds.<T>scoresOnly());
	}


	/**
	 * Takes an iterable of scored items and makes a map from items to their scores.  The items should have properly
	 * defined {@code hashCode} and {@code equals}.  If the same item appears multiple times with different scores,
	 * the highest is kept.
	 *
	 * @param scoredItems
	 * @return
	 */
	public static <T> ImmutableMap<T, Double> asMapKeepingHigestScore(final Iterable<Scored<T>> scoredItems) {
		final Map<T, Double> ret = Maps.newHashMap();

		for (final Scored<T> scoredItem : scoredItems) {
			final Double curScore = ret.get(scoredItem.item());
			if (curScore != null) {
				if (scoredItem.score() > curScore) {
					ret.put(scoredItem.item(), scoredItem.score());
				}
			} else {
				ret.put(scoredItem.item(), scoredItem.score());
			}
		}

		return ImmutableMap.copyOf(ret);
	}
}
