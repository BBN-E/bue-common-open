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
      ret += Math.exp(ret - maxVal);
    }
    return maxVal + Math.log(ret);
  }
}
