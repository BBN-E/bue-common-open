package com.bbn.bue.common.collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utilities for working with {@link java.util.List}s.
 */
public final class ListUtils {
  private ListUtils() {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns an unmodifiable view of the concatenation of two lists.
   * This view will act like a {@link List} which has all of the items of
   * {@code first} followed by all of the items in {@code second}.
   */
  public static <E> List<E> concat(List<? extends E> first, List<? extends E> second) {
    return new ConcatenatedListView<E>(first, second);
  }

  /**
   * Returns a shuffled copy of the provided list.
   *
   * @param list the list to shuffle
   * @param rng random number generator to use
   * @return a shuffled copy of the list
   */
  public static <E> ImmutableList<E> shuffledCopy(List<? extends E> list, Random rng) {
    final ArrayList<E> shuffled = Lists.newArrayList(list);
    Collections.shuffle(shuffled, rng);
    return ImmutableList.copyOf(shuffled);
  }

  /**
   * See {@link #concat(List, List)}.
   */
  private static final class ConcatenatedListView<E> extends AbstractList<E> {

    private final List<? extends E> list1;
    private final List<? extends E> list2;

    ConcatenatedListView(final List<? extends E> list1, final List<? extends E> list2) {
      // no defensive copies because this is a view
      this.list1 = checkNotNull(list1);
      this.list2 = checkNotNull(list2);
    }

    @Override
    public E get(final int index) {
      final boolean useFirstList = index < list1.size();
      final List<? extends E> list = useFirstList ? list1 : list2;
      final int modifiedIdx = useFirstList ? index : index - list1.size();
      return list.get(modifiedIdx);
    }

    @Override
    public int size() {
      return list1.size() + list2.size();
    }
  }
}
