package com.bbn.bue.common.collections;

import java.util.AbstractList;
import java.util.List;

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
  public static <E> List<E> concat(List<E> first, List<E> second) {
    return new ConcatenatedListView<E>(first, second);
  }

  /**
   * See {@link #concat(List, List)}.
   */
  private static final class ConcatenatedListView<E> extends AbstractList<E> {
    private final List<E> list1;
    private final List<E> list2;

    ConcatenatedListView(final List<E> list1, final List<E> list2) {
      // no defensive copies because this is a view
      this.list1 = checkNotNull(list1);
      this.list2 = checkNotNull(list2);
    }

    @Override
    public E get(final int index) {
      final boolean useFirstList = index < list1.size();
      final List<E> list = useFirstList ? list1 : list2;
      final int modifiedIdx = useFirstList ? index : index - list1.size();
      return list.get(modifiedIdx);
    }

    @Override
    public int size() {
      return list1.size() + list2.size();
    }
  }
}
