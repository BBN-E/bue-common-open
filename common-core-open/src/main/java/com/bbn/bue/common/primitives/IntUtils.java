package com.bbn.bue.common.primitives;

import com.google.common.base.Function;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility methods for working with primitive integers.
 *
 * @author Ryan Gabbard
 */
public final class IntUtils {

  private IntUtils() {
    throw new UnsupportedOperationException();
  }


  /**
   * Fisher-Yates suffer a primitive int array
   */
  public static void shuffle(final int[] arr, final Random rng) {
    // Fisher-Yates shuffle
    for (int i = arr.length; i > 1; i--) {
      // swap i-1 and a random spot
      final int a = i - 1;
      final int b = rng.nextInt(i);

      final int tmp = arr[b];
      arr[b] = arr[a];
      arr[a] = tmp;
    }
  }

  /**
   * Produces an array of integers from 0 (inclusive) to len (exclusive)
   */
  public static int[] arange(final int len) {
    checkArgument(len > 0);
    final int[] ret = new int[len];
    for (int i = 0; i < len; ++i) {
      ret[i] = i;
    }
    return ret;
  }


  public static void writeTo(final int[] arr, final DataOutputStream out) throws IOException {
    out.writeInt(arr.length);
    for (final int x : arr) {
      out.writeInt(x);
    }
  }

  public static int[] readIntegerArrayFrom(final DataInputStream in) throws IOException {
    final int size = in.readInt();
    final int[] ret = new int[size];
    for (int i = 0; i < size; ++i) {
      ret[i] = in.readInt();
    }
    return ret;
  }

  /**
   * Note: this does not warn about overflow beyond the range of a long.
   */
  public static Long sum(Iterable<Integer> values) {
    long ret = 0;
    for (final int x : values) {
      ret += x;
    }
    return ret;
  }

  // deprecated methods below

  @Deprecated
  public static final Function<Integer, Integer> IncrementInteger =
      new Function<Integer, Integer>() {
        @Override
        public Integer apply(final Integer i) {
          return i + 1;
        }
      };

  @Deprecated
  public static Function<Integer, Integer> SubtractInteger(final int amount) {
    return new Function<Integer, Integer>() {
      @Override
      public Integer apply(final Integer x) {
        return x - amount;
      }
    };
  }

  @Deprecated
  public static final Function<Integer, String> IntToString = new Function<Integer, String>() {
    @Override
    public String apply(final Integer x) {
      return Integer.toString(x);
    }
  };

  @Deprecated
  public static final Function<String, Integer> StringToInt = new Function<String, Integer>() {
    @Override
    public Integer apply(final String x) {
      return Integer.parseInt(x);
    }
  };

  /**
   * Produces a permutation of the integers from 0 (inclusive) to numElements (exclusive) which can
   * then be used to permute other things.
   *
   * @deprecated Use {@link com.bbn.bue.common.math.Permutation#createForNElements(int, Random)}
   * instead, which prevents bugs due to being inconsistent about how to interpret the permutation
   * array.
   */
  @Deprecated
  public static int[] permutation(final int numElements, final Random rng) {
    final int[] permutation = arange(numElements);
    shuffle(permutation, checkNotNull(rng));
    return permutation;
  }

  /**
   * Permutes an array according to the specified permutation. Use Permutation.permute(int[])
   * instead.
   *
   * @deprecated
   */
  @Deprecated
  public static void permute(final int[] arr, final int[] permutation) {
    checkArgument(arr.length == permutation.length);
    final int[] tmp = new int[arr.length];
    for (int i = 0; i < tmp.length; ++i) {
      tmp[i] = arr[permutation[i]];
    }
    System.arraycopy(tmp, 0, arr, 0, arr.length);
  }

}
