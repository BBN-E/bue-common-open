package com.bbn.bue.common.hppc.permutationProxies;

import com.bbn.bue.common.math.permutationProxies.PermutationProxy;

import com.carrotsearch.hppc.DoubleArrayList;
import com.carrotsearch.hppc.IntArrayList;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class PairedIntDoubleArrayListPermutationProxy implements PermutationProxy {

  private PairedIntDoubleArrayListPermutationProxy(final IntArrayList intArr,
      final DoubleArrayList doubleArr,
      final int startIdxInclusive, final int endIdxExclusive) {
    checkArgument(startIdxInclusive >= 0);
    checkArgument(startIdxInclusive <= endIdxExclusive);
    checkArgument(endIdxExclusive <= intArr.size());
    checkArgument(intArr.size() == doubleArr.size());
    this.intArr = checkNotNull(intArr);
    this.doubleArr = checkNotNull(doubleArr);
    this.bufferFilled = false;
    this.startIdx = startIdxInclusive;
    this.length = endIdxExclusive - startIdxInclusive;
  }

  public static PairedIntDoubleArrayListPermutationProxy createForArrayListSlices(
      final IntArrayList intArr, final DoubleArrayList doubleArr,
      final int startIdxInclusive, final int endIdxExclusive) {
    return new PairedIntDoubleArrayListPermutationProxy(intArr, doubleArr,
        startIdxInclusive, endIdxExclusive);
  }

  public static PairedIntDoubleArrayListPermutationProxy createForArrays(
      final IntArrayList intArr, final DoubleArrayList doubleArr) {
    return new PairedIntDoubleArrayListPermutationProxy(intArr, doubleArr,
        0, intArr.size());
  }

  @Override
  public void shiftIntoTemporaryBufferFrom(final int srcIdx) {
    if (bufferFilled) {
      throw new RuntimeException("Trying to shift into full buffer!");
    }
    bufferFilled = true;
    this.tmpInt = intArr.get(startIdx + srcIdx);
    this.tmpDouble = doubleArr.get(startIdx + srcIdx);
  }

  @Override
  public void shiftOutOfTemporaryBufferTo(final int destIdx) {
    if (!bufferFilled) {
      throw new RuntimeException("Trying to shift out of empty buffer!");
    }
    bufferFilled = false;
    intArr.set(startIdx + destIdx, tmpInt);
    doubleArr.set(startIdx + destIdx, tmpDouble);
  }

  @Override
  public void shift(final int srcIdx, final int destIdx) {
    intArr.set(startIdx + destIdx, intArr.get(startIdx + srcIdx));
    doubleArr.set(startIdx + destIdx, doubleArr.get(startIdx + srcIdx));
  }

  @Override
  public int length() {
    return length;
  }

  private final IntArrayList intArr;
  private final DoubleArrayList doubleArr;
  private int tmpInt;
  private final int startIdx;
  private final int length;

  private double tmpDouble;
  private boolean bufferFilled;
}
