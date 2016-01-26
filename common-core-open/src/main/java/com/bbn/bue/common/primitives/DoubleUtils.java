package com.bbn.bue.common.primitives;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.primitives.Doubles;

import static com.google.common.base.Preconditions.checkArgument;

public final class DoubleUtils {

  private DoubleUtils() {
    throw new UnsupportedOperationException();
  }

  public static double dotProduct(final double[] a, final double[] b) {
    checkArgument(a.length == b.length);
    double ret = 0.0;
    for (int i = 0; i < a.length; ++i) {
      ret += a[i] * b[i];
    }
    return ret;
  }

  public static final Predicate<Double> IsFinite = new Predicate<Double>() {
    @Override
    public boolean apply(final Double x) {
      return Doubles.isFinite(x);
    }
  };

  public static final Predicate<Double> IsNonNegative = new Predicate<Double>() {
    @Override
    public boolean apply(final Double x) {
      return x >= 0.0;
    }
  };


  public static final Function<String, Double> ParseDouble = new Function<String, Double>() {
    @Override
    public Double apply(final String x) {
      return Double.parseDouble(x);
    }
  };


  /**
   * Sum an array of doubles
   */
  public static double sum(final double[] data) {
    double ret = 0.0;
    for (final double x : data) {
      ret += x;
    }
    return ret;
  }

  public static double sum(final double[] data, final int startInclusive, final int endExclusive) {
    checkArgument(startInclusive >= 0);
    checkArgument(endExclusive <= data.length);
    checkArgument(endExclusive >= startInclusive);

    double ret = 0.0;
    for (int i = startInclusive; i < endExclusive; ++i) {
      ret += data[i];
    }
    return ret;
  }

  /**
   * Returns the index of the first minimal element of the array. That is, if there is a unique
   * minimum, its index is returned. If there are multiple values tied for smallest, the index of
   * the first is returned. If the supplied array is empty, an {@link IllegalArgumentException} is
   * thrown.
   */
  public static int argMin(final double[] x) {
    checkArgument(x.length > 0);
    double minValue = Double.MAX_VALUE;
    int minIndex = 0;

    for (int i = 0; i < x.length; ++i) {
      final double val = x[i];
      if (val < minValue) {
        minIndex = i;
        minValue = val;
      }
    }
    return minIndex;
  }

  /**
   * Returns the index of the first minimal element of the array within the specified bounds. That
   * is, if there is a unique minimum, its index is returned. If there are multiple values tied for
   * smallest, the index of the first is returned. If the supplied array is empty, an {@link
   * IllegalArgumentException} is thrown.
   */
  public static int argMin(final double[] x, final int startInclusive, final int endExclusive) {
    checkArgument(endExclusive > startInclusive);
    checkArgument(startInclusive >= 0);
    checkArgument(endExclusive <= x.length);
    double minValue = Double.MAX_VALUE;
    int minIndex = 0;

    for (int i = startInclusive; i < endExclusive; ++i) {
      final double val = x[i];
      if (val < minValue) {
        minIndex = i;
        minValue = val;
      }
    }
    return minIndex;
  }


  /**
   * Returns the index of the first maximal element of the array. That is, if there is a unique
   * maximum, its index is returned. If there are multiple values tied for largest, the index of the
   * first is returned. If the supplied array is empty, an {@link IllegalArgumentException} is
   * thrown.
   */
  public static int argMax(final double[] x) {
    checkArgument(x.length > 0);
    double maxValue = Double.MIN_VALUE;
    int maxIndex = 0;

    for (int i = 0; i < x.length; ++i) {
      final double val = x[i];
      if (val > maxValue) {
        maxIndex = i;
        maxValue = val;
      }
    }
    return maxIndex;
  }


  /**
   * Returns the index of the first maximal element of the array within the specified bounds. That
   * is, if there is a unique maximum, its index is returned. If there are multiple values tied for
   * largest, the index of the first is returned. If the supplied array is empty, an {@link
   * IllegalArgumentException} is thrown.
   */
  public static int argMax(final double[] x, final int startInclusive, final int endExclusive) {
    checkArgument(endExclusive > startInclusive);
    checkArgument(startInclusive >= 0);
    checkArgument(endExclusive <= x.length);

    double maxValue = Double.MIN_VALUE;
    int maxIndex = 0;

    for (int i = startInclusive; i < endExclusive; ++i) {
      final double val = x[i];
      if (val > maxValue) {
        maxIndex = i;
        maxValue = val;
      }
    }
    return maxIndex;
  }

  /**
   * Returns the maximum value in the array within the specified bounds. If the supplied range is
   * empty or invalid, an {@link IllegalArgumentException} is thrown.
   */
  public static double max(final double[] data, final int startInclusive, final int endExclusive) {
    checkArgument(endExclusive > startInclusive);
    checkArgument(startInclusive >= 0);
    checkArgument(endExclusive <= data.length);

    double maxValue = Double.NEGATIVE_INFINITY;
    for (int i = startInclusive; i < endExclusive; ++i) {
      maxValue = Math.max(maxValue, data[i]);
    }
    return maxValue;
  }

  /**
   * Returns the maximum value in the array within the specified bounds. If the supplied range is
   * empty or invalid, an {@link IllegalArgumentException} is thrown.
   */
  public static double min(final double[] data, final int startInclusive, final int endExclusive) {
    checkArgument(endExclusive > startInclusive);
    checkArgument(startInclusive >= 0);
    checkArgument(endExclusive <= data.length);

    double minValue = Double.POSITIVE_INFINITY;
    for (int i = startInclusive; i < endExclusive; ++i) {
      minValue = Math.min(minValue, data[i]);
    }
    return minValue;
  }


  public static boolean isCloseToIntegral(final double value, final double tolerance) {
    return Math.abs(value - Math.round(value)) <= tolerance;
  }

  /**
   * Returns true if the two provided values are within {@code tolerance} of one another.
   */
  public static boolean withinEpsilonOf(final double value1, final double value2,
      final double tolerance) {
    return Math.abs(value1 - value2) < tolerance;
  }

  /**
   * Sums an iterable of non-null {@code Doubles}.
   */
  public static double sum(final Iterable<Double> values) {
    double ret = 0.0;
    for (final double d : values) {
      ret += d;
    }
    return ret;
  }

  /**
   * Calculates {@code log(sum_i(exp(x_i)))} in a more numerically stable way than the naive
   * implementation.  Such sums commonly arise in machine learning algorithms (e.g. calculating
   * expectations in conditional random fields). If the input array is empty, {@link
   * Double#NEGATIVE_INFINITY} is returned.
   */
  public static double logSumOfExponentials(double[] arr) {
    if (arr.length == 0) {
      return Double.NEGATIVE_INFINITY;
    }

    final double maxVal = Doubles.max(arr);
    double ret = 0.0;
    for (int i = 0; i < arr.length; ++i) {
      // ensures biggest value we ever exp is 0.
      ret += Math.exp(arr[i] - maxVal);
    }
    return maxVal + Math.log(ret);
  }


  /**
   * Shifts the provided {@code val} towards but not past zero.  If the absolute value of
   * {@code val} is less than or equal to shift, zero will be returned. Otherwise, negative {@code val}s
   * will have {@code shift} added and positive vals will have {@code shift} subtracted.
   *
   * {@code shift} must be non-negative
   *
   * Inspired by AdaGradRDA.ISTAHelper from FACTORIE.
   */
  public static double shiftTowardsZeroWithClipping(double val, double shift) {
    checkArgument(shift>=0.0);
    if (val > shift) {
      return val - shift;
    } else if (val < -shift) {
      return val + shift;
    } else {
      return 0.0;
    }
  }

  /**
   * Shifts the provided {@code val} towards but not past zero.  If the absolute value of {@code
   * val} is less than or equal to shift, zero will be returned. Otherwise, negative {@code val}s
   * will have {@code shift} added and positive vals will have {@code shift} subtracted.
   *
   * If {@code shift} is negative, the result is undefined.  This method is the same as {@link
   * #shiftTowardsZeroWithClipping(double, double)} except that it eliminates the check on {@code
   * shift} for speed in deep-inner loops. This is a profile/jitwatch-guided optimization.
   *
   * Inspired by AdaGradRDA.ISTAHelper from FACTORIE.
   */
  public static double shiftTowardsZeroWithClippingRecklessly(double val, double shift) {
    if (val > shift) {
      return val - shift;
    } else if (val < -shift) {
      return val + shift;
    } else {
      return 0.0;
    }
  }

  /**
   * Clips the given value within the given bounds.  If {@code -bounds <=  val <= bounds}, {@code
   * val} is returned unchanged. Otherwise, {@code -bounds} is returned if {@code val<bounds} and
   * {@code bounds} is returned if {@code val>bounds}. If {@code bounds} is negative, an {@link
   * IllegalArgumentException} will be thrown.  In inner loops, consider using {@link
   * #clipRecklessly(double, double)}.
   *
   * {@code NaN} values will be left unchanged, but positive and negative infinity will be clipped.
   */
  public static double clip(double val, double bounds) {
    checkArgument(bounds >= 0.0);
    return clipRecklessly(val, bounds);
  }

  /**
   * Clips the given value within the given bounds.  If {@code -bounds <=  val <= bounds}, {@code
   * val} is returned unchanged. Otherwise, {@code -bounds} is returned if {@code val<bounds} and
   * {@code bounds} is returned if {@code val>bounds}. {@code bounds} must be non-negative, but this
   * is not enforced, so prefer using {@link #clip(double, double)} except in inner-loops.
   *
   * {@code NaN} values will be left unchanged, but positive and negative infinity will be clipped.
   */
  public static double clipRecklessly(double val, double bounds) {
    if (val > bounds) {
      return bounds;
    } else if (val < -bounds) {
      return -bounds;
    } else {
      return val;
    }
  }
}
