package com.bbn.bue.common.collections;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultiset;

import java.util.Comparator;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Utilities for working with Guava'a {@link com.google.common.collect.Multiset}s.
 *
 * @author Ryan Gabbard
 */
public final class MultisetUtils {

  private MultisetUtils() {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns a {@link Multiset} whose elements are the counts of the elements in the input
   * {@link Multiset}.  This is most useful for generating histograms.  The histogram
   * elements will be in ascending order.
   */
  public static <T> Multiset<Integer> histogram(Multiset<T> data) {
    return histogram(data, Ordering.<Integer>natural());
  }

  /**
   * Returns a {@link Multiset} whose elements are the counts of the elements in the input
   * {@link Multiset}.  This is most useful for generating histograms.  Whether the histogram
   * elements are in ascending order or descending order is controlled by {@code comparator}.
   */
  public static <T> Multiset<Integer> histogram(Multiset<T> data,
      Comparator<? super Integer> comparator) {
    final TreeMultiset<Integer> histogram = TreeMultiset.create(comparator);

    for (final Multiset.Entry<T> entry : data.entrySet()) {
      histogram.add(entry.getCount());
    }

    return histogram;
  }

  /**
   * Retrieves all elements from a {@link Multiset} whose count is greater than or equal to
   * {@code minCount} (which must be non-negative).
   */
  public static <T> Iterable<T> elementsOccuringAtLeast(final Multiset<T> data,
      int minCount) {
    checkArgument(minCount >= 0);
    return FluentIterable.from(data.entrySet())
        .filter(MultisetUtils.<T>occursAtLeast(minCount))
        .transform(MultisetUtils.<T>elementOnly());
  }

  /**
   * Guava {@link Function} mapping a multiset entry to the element it wraps.
   */
  public static <T> Function<Multiset.Entry<T>, T> elementOnly() {
    return new ElementOnly<>();
  }

  private static final class ElementOnly<T> implements Function<Multiset.Entry<T>, T> {

    @Override
    public T apply(Multiset.Entry<T> entry) {
      return entry.getElement();
    }
  }

  /**
   * Guava {@link Predicate} on {@link Multiset.Entry} which passes only if the entry's count
   * is greater than or equal to {@code n}, which must be non-negative.
   */
  public static <T> Predicate<Multiset.Entry<T>> occursAtLeast(int n) {
    checkArgument(n >= 0);
    return new OccursAtLeast<>(n);
  }

  private static final class OccursAtLeast<T> implements Predicate<Multiset.Entry<T>> {

    OccursAtLeast(int n) {
      this.n = n;
    }

    @Override
    public boolean apply(Multiset.Entry<T> entry) {
      return entry.getCount() >= n;
    }

    private final int n;
  }

  /**
   * An ordering of {@link Multiset} elements by descending order of count, with ties broken
   * according to the supplied {@code itemComparator}.
   */
  public static <E> Ordering<Multiset.Entry<E>>
  byCountDescendingThenItemAscendingOrdering(Comparator<? super E> itemComparator) {
    final Ordering<Multiset.Entry<E>> byCount = byCountOrdering();
    final Ordering<Multiset.Entry<E>> byCountReversed = byCount.reverse();
    final Ordering<Multiset.Entry<E>> byElement = byElementOrdering(itemComparator);
    return byCountReversed.compound(byElement);
  }

  public static <ElementType> Function<Multiset.Entry<ElementType>, Integer> toCountFunction() {
    return new ToCountFunction<>();
  }

  private static class ToCountFunction<T> implements Function<Multiset.Entry<T>, Integer> {

    @Override
    public Integer apply(final Multiset.Entry<T> input) {
      return input.getCount();
    }
  }

  /**
   * Returns the partial {@link Ordering} over {@link Multiset.Entry}s resulting from applying
   * the supplied {@code comparator} to the multiset elements.
   */
  public static <ElementType> Ordering<Multiset.Entry<ElementType>> byElementOrdering(
      Comparator<? super ElementType> comparator) {
    return Ordering.from(comparator).onResultOf(MultisetUtils.<ElementType>elementOnly());
  }

  /**
   * Returns a partial {@link Ordering} over {@link Multiset.Entry}s by their count.
   */
  public static <ElementType> Ordering<Multiset.Entry<ElementType>> byCountOrdering() {
    return Ordering.natural().onResultOf(MultisetUtils.<ElementType>toCountFunction());
  }

  /**
   * Gets the maximum count of any element in a {@link Multiset}. This will be zero if the
   * {@code Multiset} is empty.
   */
  public static int maxCount(final Multiset<?> multiset) {
    int ret = 0;
    for (final Multiset.Entry<?> entry : multiset.entrySet()) {
      if (entry.getCount() > ret) {
        ret = entry.getCount();
      }
    }
    return ret;
  }
}
