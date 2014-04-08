package com.bbn.bue.common.scoring;

import com.google.common.base.Objects;


/**
 * Represents any item with an associated score. Note that this class intentionally
 * does not implement comparable to avoid only working with comparable wrapped types.
 * If you need a comparator, use either Scoreds.ByScoreThenByItem or
 * Scoreds.ByScoreOnly.
 *
 * @author rgabbard
 *
 * @param <T>
 */
public final class Scored<T> {
	public Scored(final T item, final double score) {
        this.item = item;
        this.score = score;
    }

    public static <T> Scored<T> from(final T item, final double score) {
    	return new Scored<T>(item, score);
    }

    public double score() {return score;};
    public T item() { return item; };

    private final T item;
    private final double score;

    @Override
	public String toString() {
    	return String.format("%s=%s", item, score);
    }

    @Override
	public int hashCode() {
    	return Objects.hashCode(item, score);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Scored<?> other = (Scored<?>) obj;
		return Objects.equal(item, other.item) &&
			score == other.score;
	}


};
