package com.bbn.bue.common.temporal;

import org.joda.time.Interval;
import org.joda.time.Period;

/**
 * Utilities for working with JodaTime {@link Interval}s.
 */
public final class IntervalUtils {

  private IntervalUtils() {
    throw new UnsupportedOperationException();
  }

  /**
   * Move both the start and end of an {@link Interval} into the past by the duration of
   * {@code period}.
   */
  public static Interval minus(final Interval interval, final Period period) {
    return new Interval(interval.getStart().minus(period),
        interval.getEnd().minus(period));
  }

  /**
   * Move both the start and end of an {@link Interval} into the future by the duration of
   * {@code period}.
   */
  public static Interval plus(final Interval interval, final Period period) {
    return new Interval(interval.getStart().plus(period),
        interval.getEnd().plus(period));
  }

  /**
   * Is {@code interval} contained entirely within one calendar day?
   */
  public static boolean containedInDay(final Interval interval) {
    return interval.getStart().dayOfMonth().toInterval().contains(interval);
  }

  /**
   * Is {@code interval} contained entirely within one calendar month?
   */
  public static boolean containedInMonth(final Interval interval) {
    return interval.getStart().monthOfYear().toInterval().contains(interval);
  }

  /**
   * Is {@code interval} contained entirely within one calendar year?
   */
  public static boolean containedInYear(final Interval interval) {
    return interval.getStart().year().toInterval().contains(interval);
  }
}
