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
 * every time an iterator is requested. Note this must its entire underlying
 * {@link Iterable} into memory.
 * @param <T>
 */
public final class ShufflingIterable<T> implements Iterable<T> {
  private final ImmutableList<T> data;
  private final Random rng;

  private ShufflingIterable(Iterable<T> iterable, Random rng) {
    this.data = ImmutableList.copyOf(iterable);
    this.rng = checkNotNull(rng);
  }

  public static <T> ShufflingIterable<T> from(Iterable<T> iterable, Random rng) {
    return new ShufflingIterable<T>(iterable, rng);
  }

  @Override
  public Iterator<T> iterator() {
    final List<T> shuffledList = Lists.newArrayList(data);
    Collections.shuffle(shuffledList);
    return Collections.unmodifiableList(shuffledList).iterator();
  }
}
