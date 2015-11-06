package com.bbn.bue.common.collections;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Table;
import com.google.common.math.IntMath;

import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

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
   * The Collections must be disjoint or an {@link java.lang.IllegalArgumentException} will be
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
   * Takes some collections and creates a {@link com.google.common.collect.ListMultimap} from their
   * elements to which collections contain them.  Unlike {@link #makeElementsToContainersMap(Iterable)},
   * the same element may appear in multiple collections. However, {@code null} may not appear in
   * any of the collections.  The returned multimap is a {@link com.google.common.collect.ListMultimap}
   * to avoid having to do potentially expensive comparisons between the sets; we know there will be
   * no duplicates because the input collections are sets.
   */
  public static <V, C extends Set<? extends V>> ImmutableListMultimap<V, C> makeSetElementsToContainersMultimap(
      final Iterable<C> sets) {
    // because these are sets we can safely use a list multimap without having to worry
    // about duplicates
    final ImmutableListMultimap.Builder<V, C> ret = ImmutableListMultimap.builder();

    for (final C set : sets) {
      for (final V item : set) {
        ret.put(item, set);
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

  /**
   * A copy of the input as an {@link com.google.common.collect.ImmutableList} which respects
   * iteration order, but where only the first occurrence of each element is kept. No input items
   * may be {@code null}.
   */
  public static <T> ImmutableList<T> asUniquedList(Iterable<T> items) {
    return ImmutableSet.copyOf(items).asList();
  }

  /**
   * Turns null into an empty list and leaves other inputs untouched.
   */
  public static <T> List<T> coerceNullToEmpty(List<T> list) {
    return MoreObjects.firstNonNull(list, ImmutableList.<T>of());
  }

  /**
   * A Guava {@link Predicate} which calls {@link Collection#isEmpty()} on provided collections.
   */
  public static Predicate<Collection<?>> isEmptyPredicate() {
    return new Predicate<Collection<?>>() {
      @Override
      public boolean apply(final Collection<?> input) {
        return input.isEmpty();
      }
    };
  }

  /**
   * Partitions a list into the specified number of partitions as evenly as is possible. The final
   * "extra" elements that cannot be evenly distributed are distributed starting with the first
   * partitions. For example, three partitions of (1, 2, 3, 4) results in ((1, 4), (2), (3)).
   * Unlike {@link Lists#partition(List, int)}, this returns {@link ImmutableList}s, not list views,
   * and computations are computed eagerly.
   *
   * @param partitions the number of partitions to divide the list into
   * @return a list of the partitions, which are themselves lists
   */
  public static <E> ImmutableList<ImmutableList<E>> partitionAlmostEvenly(final List<E> list,
      final int partitions) {
    checkNotNull(list);
    checkArgument(partitions > 0,
        "Number of partitions must be positive");
    checkArgument(partitions <= list.size(),
        "Cannot request more partitions than there are list items");

    // Divide into partitions, with the remainder falling into the last partition
    final List<List<E>> prelimPartitions =
        Lists.partition(list, IntMath.divide(list.size(), partitions, RoundingMode.DOWN));
    // Create output
    final ImmutableList.Builder<ImmutableList<E>> ret = ImmutableList.builder();

    // If we evenly partitioned, just do the type conversion and return. The type conversion is
    // performed because Lists#partition returns list views.
    if (prelimPartitions.size() == partitions) {
      for (List<E> partition : prelimPartitions) {
        ret.add(ImmutableList.copyOf(partition));
      }
    } else {
      // Otherwise, distribute the extras
      final ImmutableList.Builder<ImmutableList.Builder<E>> builderOfBuilders =
          ImmutableList.builder();

      // Make new builder for all but the extras, which come from the last index
      final int lastIdx = prelimPartitions.size() - 1;
      final List<E> extras = prelimPartitions.get(lastIdx);
      for (int i = 0; i < lastIdx; i++) {
        builderOfBuilders.add(ImmutableList.<E>builder().addAll(prelimPartitions.get(i)));
      }
      final ImmutableList<ImmutableList.Builder<E>> builders = builderOfBuilders.build();

      // Distribute the extra elements. We cannot overrun the bounds of builders because the number
      // of extras is always at least one less than the number of builders (otherwise, we would've
      // just had larger partitions).
      int partitionIdx = 0;
      for (E item : extras) {
        builders.get(partitionIdx++).add(item);
      }

      // Fill in output
      for (ImmutableList.Builder<E> builder : builders) {
        ret.add(builder.build());
      }
    }
    return ret.build();
  }
}
