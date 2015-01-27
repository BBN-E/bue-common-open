package com.bbn.bue.common.math;

import static com.google.common.base.Preconditions.checkArgument;

public final class MathUtils {

  private MathUtils() {
    throw new UnsupportedOperationException();
  }

  public static int max(int[] arr) {
    checkArgument(arr.length > 0);
    int mx = Integer.MIN_VALUE;

    for (final int x : arr) {
      if (x > mx) {
        mx = x;
      }
    }
    return mx;
  }

  public static int sum(int[] permutation) {
    int ret = 0;
    for (final int x : permutation) {
      ret += x;
    }
    return ret;
  }

  public static double xLogX(double d) {
    if (d == 0.0) {
      return 0.0;
    } else {
      return d * Math.log(d);
    }
  }
}
