package com.bbn.bue.common.collections;

import com.google.common.base.Function;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import com.google.common.collect.Table;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utilities for collections.
 *
 * @author rgabbard
 */
public final class CollectionUtils {

  private CollectionUtils() {
    throw new UnsupportedOperationException();
  }

  /**
   * Takes some collections and creates a map from their elements to which collections contain them.
   *  The Collections must be disjoint or an {@link java.lang.IllegalArgumentException} will be
   * thrown.
   */
  public static <T, C extends Collection<T>> Map<T, C> makeElementsToContainersMap(
      final Iterable<C> collections) {
    final ImmutableMap.Builder<T, C> ret = ImmutableMap.builder();

    for (final C collection : collections) {
      for (final T item : collection) {
        ret.put(item, collection);
      }
    }

    return ret.build();
  }

  /**
   * Returns a new Multiset resulting from transforming each element of the input Multiset by a
   * function. If two or more elements are mapped to the same value by the function, their counts
   * will be summed in the new Multiset.
   */
  public static <A, B> ImmutableMultiset<B> transformedCopy(Multiset<A> ms,
      Function<A, B> func) {
    final ImmutableMultiset.Builder<B> ret = ImmutableMultiset.builder();

    for (final Multiset.Entry<A> entry : ms.entrySet()) {
      final B transformedElement = func.apply(entry.getElement());
      ret.addCopies(transformedElement, entry.getCount());
    }

    return ret.build();
  }

  /**
   * Same as transformedCopy, except the returned Multiset is mutable.
   */
  public static <A, B> Multiset<B> mutableTransformedCopy(Multiset<A> ms,
      Function<A, B> func) {
    final Multiset<B> ret = HashMultiset.create();

    for (final Multiset.Entry<A> entry : ms.entrySet()) {
      final B transformedElement = func.apply(entry.getElement());
      ret.add(transformedElement, entry.getCount());
    }

    return ret;
  }

  /**
   * Gets a sublist of a list, truncated at the end of the list if too many elements are selected.
   * This behaves exactly like List.subList, including all notes in its Javadoc concerning
   * structural modification of the backing List, etc. with one difference: if the end index is
   * beyond the end of the list, instead of throwing an exception, the sublist simply stops at the
   * end of the list.  After the fifth or so time writing this idiom, it seems worth having a
   * function for. :-)
   */
  public static <T> List<T> truncatedSubList(List<T> inList, int start, int end) {
    // List.sublist will do our error checking for us
    final int limit = Math.min(end, inList.size());
    return inList.subList(start, limit);
  }

  /**
   * Returns true if and only if all the collections in the provided list have the same size.
   * Returns true if the provided list is empty.
   */
  public static boolean allSameSize(List<Collection<?>> collections) {
    if (collections.isEmpty()) {
      return true;
    }
    final int referenceSize = collections.get(0).size();
    for (final Collection<?> col : collections) {
      if (col.size() != referenceSize) {
        return false;
      }
    }
    return true;
  }

  /**
   * Guava function to get the value of a {@link com.google.common.collect.Table} cell.
   */
  public static <V> Function<Table.Cell<?, ?, V>, V> TableCellValue() {
    return new Function<Table.Cell<?, ?, V>, V>() {
      @Override
      public V apply(Table.Cell<?, ?, V> input) {
        return input.getValue();
      }
    };
  }

  /**
   * Guava function to map a collection to its size. Prefer {@link #sizeFunction()}.
   *
   * @deprecated
   */
  public static final Function<Collection<?>, Integer> Size =
      new Function<Collection<?>, Integer>() {
        @Override
        public Integer apply(Collection<?> input) {
          return input.size();
        }
      };

  /**
   * Guava function to map a collection to its size.
   */
  public static final Function<Collection<?>, Integer> sizeFunction() {
    return Size;
  }

  /**
   * Like {@link Collections#max(java.util.Collection)} except with a default value returned in the
   * case of an empty collection.
   */
  public static <T extends Comparable<T>> T maxOr(Collection<T> values, T defaultVal) {
    if (values.isEmpty()) {
      return defaultVal;
    } else {
      return Collections.max(values);
    }
  }

  /**
   * Like {@link Collections#min(java.util.Collection)} except with a default value returned in the
   * case of an empty collection.
   */
  public static <T extends Comparable<T>> T minOr(Collection<T> values, T defaultVal) {
    if (values.isEmpty()) {
      return defaultVal;
    } else {
      return Collections.min(values);
    }
  }

  /**
   * Provides a {@link com.google.common.base.Function} which will create a set containing the same
   * elements as the supplied collection.
   */
  public static <T> Function<Collection<T>, ImmutableSet<T>> asSetFunction() {
    return new Function<Collection<T>, ImmutableSet<T>>() {
      @Override
      public ImmutableSet<T> apply(Collection<T> input) {
        return ImmutableSet.copyOf(input);
      }
    };
  }
}
