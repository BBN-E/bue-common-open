package com.bbn.bue.common.hppc.permutationProxies;

import com.bbn.bue.common.math.permutationProxies.PermutationProxy;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.ShortArrayList;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link PermutationProxy} for a parallel pair of {@link IntArrayList} and {@link ShortArrayList}
 * which should be permuted together.
 */
public final class PairedIntShortArrayListPermutationProxy implements PermutationProxy {

  private final IntArrayList intArr;
  private final ShortArrayList shortArr;
  private int tmpInt;
  private final int startIdx;
  private final int length;

  private short tmpShort;
  private boolean bufferFilled;

  private PairedIntShortArrayListPermutationProxy(final IntArrayList intArr,
      final ShortArrayList shortArr,
      final int startIdxInclusive, final int endIdxExclusive) {
    checkArgument(startIdxInclusive >= 0);
    checkArgument(startIdxInclusive <= endIdxExclusive);
    checkArgument(endIdxExclusive <= intArr.size());
    checkArgument(intArr.size() == shortArr.size());
    this.intArr = checkNotNull(intArr);
    this.shortArr = checkNotNull(shortArr);
    this.bufferFilled = false;
    this.startIdx = startIdxInclusive;
    this.length = endIdxExclusive - startIdxInclusive;
  }

  public static PairedIntShortArrayListPermutationProxy createForArrayListSlices(
      final IntArrayList intArr, final ShortArrayList shortArr,
      final int startIdxInclusive, final int endIdxExclusive) {
    return new PairedIntShortArrayListPermutationProxy(intArr, shortArr,
        startIdxInclusive, endIdxExclusive);
  }

  public static PairedIntShortArrayListPermutationProxy createForArrays(
      final IntArrayList intArr, final ShortArrayList shortArr) {
    return new PairedIntShortArrayListPermutationProxy(intArr, shortArr,
        0, intArr.size());
  }

  @Override
  public void shiftIntoTemporaryBufferFrom(final int srcIdx) {
    if (bufferFilled) {
      throw new RuntimeException("Trying to shift into full buffer!");
    }
    bufferFilled = true;
    this.tmpInt = intArr.get(startIdx + srcIdx);
    this.tmpShort = shortArr.get(startIdx + srcIdx);
  }

  @Override
  public void shiftOutOfTemporaryBufferTo(final int destIdx) {
    if (!bufferFilled) {
      throw new RuntimeException("Trying to shift out of empty buffer!");
    }
    bufferFilled = false;
    intArr.set(startIdx + destIdx, tmpInt);
    shortArr.set(startIdx + destIdx, tmpShort);
  }

  @Override
  public void shift(final int srcIdx, final int destIdx) {
    intArr.set(startIdx + destIdx, intArr.get(startIdx + srcIdx));
    shortArr.set(startIdx + destIdx, shortArr.get(startIdx + srcIdx));
  }

  @Override
  public int length() {
    return length;
  }
}
