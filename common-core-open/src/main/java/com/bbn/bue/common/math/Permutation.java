package com.bbn.bue.common.math;

import com.bbn.bue.common.math.permutationProxies.PairedIntDoubleArrayPermutationProxy;
import com.bbn.bue.common.math.permutationProxies.PermutationProxy;
import com.bbn.bue.common.primitives.IntUtils;

import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a permutation of elements of some sequence
 *
 * @author Ryan Gabbard
 */
public final class Permutation {

  private Permutation(final int[] sources) {
    this.sources = checkNotNull(sources);
    this.destinations = destinationsFromSources(sources);
  }

  /**
   * For *post-permutation* index {@code idx}, what *pre-permutation* index did it come from?
   */
  public int sourceOfIndex(final int idx) {
    return sources[idx];
  }

  /**
   * For *pre-permutation* index {@code idx}, what index will it end up at *post-permutation*?
   */
  public int destinationOfIndex(final int index) {
    return destinations[index];
  }

  /**
   * Creates a random permutation of n elements using the supplied random number generator.  Note
   * that for all but small numbers of elements most possible permutations will not be sampled by
   * this because the random generator's space is much smaller than the number of possible
   * permutations.
   */
  public static Permutation createForNElements(final int numElements, final Random rng) {
    final int[] permutation = IntUtils.arange(numElements);
    IntUtils.shuffle(permutation, checkNotNull(rng));
    return new Permutation(permutation);
  }

  /**
   * Creates a Permutation from sort indices returned by HPPC's IndirectSort.  No validity checking
   * is done, so this *must* be called with the sort indices from an HPPC indirect sort using the
   * same start index passed to that sort.  If you do otherwise, you will end up with a invalid
   * permutation with undefined behavior.
   */
  public static Permutation fromSortIndicesUnchecked(final int[] sortIndices,
      final int startIndex) {
    final int[] sources = sortIndices.clone();
    for (int i = 0; i < sources.length; ++i) {
      sources[i] -= startIndex;
    }
    return new Permutation(sources);
  }

  /**
   * Applies this permutation in-place to the elements of an integer array
   */
  public void permute(final int[] arr) {
    checkArgument(arr.length == sources.length);
    final int[] tmp = new int[arr.length];
    for (int i = 0; i < tmp.length; ++i) {
      tmp[i] = arr[sources[i]];
    }
    System.arraycopy(tmp, 0, arr, 0, arr.length);
  }

  /**
   * Applies this permutation in-place to a paired integer and double array.
   */
  public void permutePaired(final int[] intArr, final double[] doubleArr) {
    permute(PairedIntDoubleArrayPermutationProxy.createForArrays(intArr, doubleArr));
  }

  /**
   * Provides a generic means of permuting things by way of a {@link PermutationProxy}. The
   * permutation proxy defines how to shift data around and store temporary data for a particular
   * data type while this method knows how to do the general logic of permuting.
   */
  public void permute(final PermutationProxy proxy) {
    checkArgument(numElements() == proxy.length());

    final boolean[] seen = new boolean[numElements()];

    for (int startIdx = 0; startIdx < seen.length; ++startIdx) {
      if (!seen[startIdx]) {
        // starting at any element, we can follow the chain of sources
        // in a cycle
        int curIdx = startIdx;
        // we have to save the data in our starting spot
        // because it will be overwritten by the time
        // we encounter the last element in the cycle, which
        // will need them
        proxy.shiftIntoTemporaryBufferFrom(startIdx);
        while (true) {
          seen[curIdx] = true;
          final int sourceIdx = sourceOfIndex(curIdx);
          if (sourceIdx == startIdx) {
            // completed the cycle
            proxy.shiftOutOfTemporaryBufferTo(curIdx);
            break;
          } else {
            // shift data from source to cur index
            proxy.shift(sourceIdx, curIdx);
            curIdx = sourceIdx;
          }
        }
      }
    }
  }

  /**
   * The number of elements this permutation applies to.
   */
  public int numElements() {
    return sources.length;
  }

  private final int[] sources;
  private final int[] destinations;

  private static int[] destinationsFromSources(final int[] sources) {
    final int[] destinations = new int[sources.length];
    for (int i = 0; i < sources.length; ++i) {
      destinations[sources[i]] = i;
    }
    return destinations;
  }

  /**
   * Creates a permutation from an array of source indices. No validity checking is done (for
   * speed), so if you supply an array which is not a permutation, behavior is undefined.
   */
  public static Permutation fromSourceIndexArrayUnchecked(final int[] sourceIndices) {
    return new Permutation(sourceIndices.clone());
  }
}
