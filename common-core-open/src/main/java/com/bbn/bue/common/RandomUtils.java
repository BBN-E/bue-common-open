package com.bbn.bue.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Utilities for dealing with randomness.
 *
 * @author Ryan Gabbard, Pamela Shapiro
 */
public final class RandomUtils {

  private RandomUtils() {
    throw new UnsupportedOperationException();
  }

  public static <T> T uniformlyRandomItem(final Random rng, final List<T> lst) {
    return lst.get(rng.nextInt(lst.size()));
  }

  /**
   * Selects {@code numInts} from the range from {@code minInclusive} to {@code maxExclusive}.
   * WARNING: the current implementation of this could be very slow if {@code numInts} is
   * close to the size of the range.  The selected ints will be returned in order from least
   * to greatest.
   */
  public static List<Integer> distinctRandomIntsInRange(final Random rng, final int minInclusive,
      final int maxExclusive, final int numInts) {
    checkArgument(numInts <= (minInclusive - maxExclusive));
    final List<Integer> chosenInts = Lists.newArrayList();

    while (chosenInts.size() < numInts) {
      final int i = rng.nextInt(maxExclusive - minInclusive);
      if (!chosenInts.contains(minInclusive + i)) {
        chosenInts.add(minInclusive + i);
      }
    }
    Collections.sort(chosenInts);
    return chosenInts;
  }

  /**
   * Given a hash-based set and a desired number of samples N, will create a new set by drawing N
   * items without replacement from the original set. This takes at least linear time in the size of
   * the source set, since there is no way to pick out arbitrary elements of a set except by
   * iteration.
   *
   * WARNING: If the desired number of samples is close to the size of the source set, the current
   * implementation could be very slow.
   */
  public static <T> Set<T> sampleHashingSetWithoutReplacement(final Set<T> sourceSet,
      final int numSamples,
      final Random rng) {
    checkArgument(numSamples <= sourceSet.size());

    // first we find the indices of the selected elements
    final List<Integer> selectedItems =
        distinctRandomIntsInRange(rng, 0, sourceSet.size(), numSamples);

    final Set<T> ret = Sets.newHashSet();
    final Iterator<Integer> selectedItemsIterator = selectedItems.iterator();

    if (numSamples > 0) {
      // now we walk through sourceSet in order to find the items at
      // the indices we selected. It doens't matter that the iteration
      // order over the set isn't guaranteed because the indices are
      // random anyway
      int nextSelectedIdx = selectedItemsIterator.next();

      int idx = 0;
      for (final T item : sourceSet) {
        if (idx == nextSelectedIdx) {
          ret.add(item);
          if (selectedItemsIterator.hasNext()) {
            nextSelectedIdx = selectedItemsIterator.next();
          } else {
            // we may (and probably will) run out of selected indices
            // before we run out of items in the set, unless the
            // last index is selected.
            break;
          }
        }
        ++idx;
      }
    }
    return ret;
  }

}
