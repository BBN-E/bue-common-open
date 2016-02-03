package com.bbn.bue.common.collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This is an {@link Iterable} which provides its data in a random order
 * every time an iterator is requested. Note this must load its entire underlying
 * {@link Iterable} into memory.
 */
public final class ShufflingIterable<T> implements Iterable<T> {
  private final ImmutableList<T> data;
  private final Random rng;

  private ShufflingIterable(final Iterable<T> iterable, final Random rng) {
    this.data = ImmutableList.copyOf(iterable);
    this.rng = checkNotNull(rng);
  }

  /**
   * Returns a new {@link ShufflingIterable} for the specified iterable and random number generator.
   *
   * @param iterable the source iterable
   * @param rng the random number generator to use in shuffling the iterable
   * @return a new shuffling iterable for contents of the original iterable
   */
  public static <T> ShufflingIterable<T> from(final Iterable<T> iterable, final Random rng) {
    return new ShufflingIterable<>(iterable, rng);
  }

  /**
   * Returns a new iterator that iterates over a new random ordering of the data.
   */
  @Override
  public Iterator<T> iterator() {
    final List<T> shuffledList = Lists.newArrayList(data);
    Collections.shuffle(shuffledList, rng);
    return Collections.unmodifiableList(shuffledList).iterator();
  }
}

