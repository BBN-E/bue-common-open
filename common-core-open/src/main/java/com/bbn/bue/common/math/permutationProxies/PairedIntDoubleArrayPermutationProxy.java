package com.bbn.bue.common.math.permutationProxies;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Ryan Gabbard
 */
public final class PairedIntDoubleArrayPermutationProxy implements PermutationProxy {

  private PairedIntDoubleArrayPermutationProxy(final int[] intArr, final double[] doubleArr,
      final int startIdxInclusive, final int endIdxExclusive) {
    checkArgument(startIdxInclusive >= 0);
    checkArgument(startIdxInclusive <= endIdxExclusive);
    checkArgument(endIdxExclusive <= intArr.length);
    checkArgument(intArr.length == doubleArr.length);
    this.intArr = checkNotNull(intArr);
    this.doubleArr = checkNotNull(doubleArr);
    this.bufferFilled = false;
    this.startIdx = startIdxInclusive;
    this.length = endIdxExclusive;
  }

  public static PairedIntDoubleArrayPermutationProxy createForArraySlices(final int[] intArr,
      final double[] doubleArr,
      final int startIdxInclusive, final int endIdxExclusive) {
    return new PairedIntDoubleArrayPermutationProxy(intArr, doubleArr,
        startIdxInclusive, endIdxExclusive);
  }

  public static PairedIntDoubleArrayPermutationProxy createForArrays(final int[] intArr,
      final double[] doubleArr) {
    return new PairedIntDoubleArrayPermutationProxy(intArr, doubleArr,
        0, intArr.length);
  }

  @Override
  public void shiftIntoTemporaryBufferFrom(final int srcIdx) {
    if (bufferFilled) {
      throw new RuntimeException("Trying to shift into full buffer!");
    }
    bufferFilled = true;
    this.tmpInt = intArr[startIdx + srcIdx];
    this.tmpDouble = doubleArr[startIdx + srcIdx];
  }

  @Override
  public void shiftOutOfTemporaryBufferTo(final int destIdx) {
    if (!bufferFilled) {
      throw new RuntimeException("Trying to shift out of empty buffer!");
    }
    bufferFilled = false;
    intArr[startIdx + destIdx] = tmpInt;
    doubleArr[startIdx + destIdx] = tmpDouble;
  }

  @Override
  public void shift(final int srcIdx, final int destIdx) {
    intArr[startIdx + destIdx] = intArr[startIdx + srcIdx];
    doubleArr[startIdx + destIdx] = doubleArr[startIdx + srcIdx];
  }

  @Override
  public int length() {
    return length;
  }

  private final int[] intArr;
  private final double[] doubleArr;
  private int tmpInt;
  private final int startIdx;
  private final int length;

  private double tmpDouble;
  private boolean bufferFilled;
}
