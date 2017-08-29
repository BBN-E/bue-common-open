package com.bbn.bue.common.collections;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.Random;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An unbounded {@link java.util.Iterator} which provides a stream of bootstrap samples
 * from a provided collection of items.  If the "base" collection is of size N, this
 * iterator will yield lists of size N where each element is sampled with replacement
 * from the base collection.
 *
 * See https://en.wikipedia.org/wiki/Bootstrapping_%28statistics%29
 *
 * {@author Ryan Gabbard}
 */
public final class BootstrapIterator<ItemType> extends AbstractIterator<Collection<ItemType>> {

  private final Random rng;
  private final ImmutableList<ItemType> data;

  private BootstrapIterator(Iterable<? extends ItemType> data, Random rng) {
    this.data = ImmutableList.copyOf(data);
    this.rng = checkNotNull(rng);
  }

  /**
   * Creates a {@code BootstrapIterator} which samples from the provided {@code data}.  This method
   * takes in a {@code Random} to ensure determinism.
   */
  public static <ItemType> BootstrapIterator<ItemType> forData(Iterable<? extends ItemType> data,
      Random rng) {
    return new BootstrapIterator<ItemType>(data, rng);
  }

  @Override
  protected ImmutableCollection<ItemType> computeNext() {
    final ImmutableList.Builder<ItemType> ret = ImmutableList.builder();

    if (data.isEmpty()) {
      return ImmutableList.of();
    }

    for (int i = 0; i < data.size(); ++i) {
      ret.add(data.get(rng.nextInt(data.size())));
    }

    return ret.build();
  }
}
