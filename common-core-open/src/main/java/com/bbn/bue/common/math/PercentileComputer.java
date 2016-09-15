package com.bbn.bue.common.math;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Computes percentiles.  There are multiple ways of computing them, so you will need to choose a
 * {@link #nistPercentileComputer()} or {@link #excelPercentileComputer()}.
 */
@Beta
public final class PercentileComputer {

  private final Algorithm algorithm;

  private PercentileComputer(Algorithm algorithm) {
    this.algorithm = checkNotNull(algorithm);
  }

  /**
   * Creates a {@code PercentileComputer} which uses the algorithm given in NIST's Engineering
   * Statistics Handbook. The relevant section is copied below:
   *
   * <blockquote> Percentiles can be estimated from {@code N} measurements as follows: for the pth
   * percentile, set p(N+1) equal to k + d for k an integer, and d, a fraction greater than or equal
   * to 0 and less than 1. <ul> <li>For 0 < k < N,  Y(p) = Y[k] + d(Y[k+1] - Y[k])</li> <li>For k =
   * 0,  Y(p) = Y[1]</li> <li>For k = N,  Y(p) = Y[N]</li> </ul> </blockquote>
   *
   * Note NIST is using 1-based indexing.
   */
  public static PercentileComputer nistPercentileComputer() {
    return new PercentileComputer(Algorithm.NIST);
  }

  /**
   * Creates a {@code PercentileComputer} which uses the "Excel" alternative algorithm given in
   * NIST's Engineering Statistics Handbook.  It is the same as the base NIST algorithm, except it
   * assigns (k,d) to the integral and fractional parts of p(N-1)+1 instead of p(N+1).
   */
  public static PercentileComputer excelPercentileComputer() {
    return new PercentileComputer(Algorithm.EXCEL);
  }

  public Percentiles calculatePercentilesAdoptingData(double[] data) {
    return new Percentiles(data);
  }

  public Percentiles calculatePercentilesCopyingData(double[] data) {
    return new Percentiles(data.clone());
  }

  // these may assume percentile is valid and data non-empty
  private enum Algorithm {
    NIST {
      @Override
      public double computePercentile(double percentile, double[] data) {
        final int N = data.length;
        final double rank = percentile * (N + 1);
        final int k = (int) rank;
        final double d = rank - k;

        if (k == 0) {
          return data[0];
        } else if (k == N) {
          return data[N - 1];
        } else {
          // we subtract 1 when looking up because NIST uses 1-based indexing
          final double yK = data[k - 1];
          final double yKPlusOne = data[k];
          return yK + d * (yKPlusOne - yK);
        }
      }
    },
    EXCEL {
      @Override
      public double computePercentile(double percentile, double[] data) {
        final int N = data.length;
        final double rank = percentile * (N - 1) + 1;
        final int k = (int) rank;
        final double d = rank - k;

        if (k == 0) {
          return data[0];
        } else if (k == N) {
          return data[N - 1];
        } else {
          // we subtract 1 when looking up because NIST uses 1-based indexing
          final double yK = data[k - 1];
          final double yKPlusOne = data[k];
          return yK + d * (yKPlusOne - yK);
        }
      }
    };

    public abstract double computePercentile(double percentile, double[] data);
  }

  /**
   * This represents the computation of percentiles on a data set.  It can be queried for various
   * percentile-related information.
   *
   * Most things returned are {@link Optional} to force the user to deal with the case of empty
   * data.
   */
  public final class Percentiles {

    @JsonProperty("data")
    final double[] data;

    Percentiles(@JsonProperty("data") double[] data) {
      this.data = data;
      Arrays.sort(data);
    }

    public int numObservedValues() {
      return data.length;
    }

    public Optional<Double> median() {
      if (data.length == 0) {
        return Optional.absent();
      }

      if (data.length % 2 == 0) {
        // if we have an event number of elements, return the mean of the two
        // middle element
        return Optional.of(0.5 * ((data[data.length / 2] + data[data.length / 2 - 1])));
      } else {
        // if we have an odd number of elements, return the unique middle element
        return Optional.of(data[data.length / 2]);
      }
    }

    public Optional<Double> min() {
      if (data.length == 0) {
        return Optional.absent();
      }

      return Optional.of(data[0]);
    }

    public Optional<Double> max() {
      if (data.length == 0) {
        return Optional.absent();
      }

      return Optional.of(data[data.length - 1]);
    }

    /**
     * Calculates the p-th percentile of the observed data.  The algorithm used varies depending on
     * what {@link PercentileComputer} generated this data. If no data was observed, this will throw
     * a {@link java.util.NoSuchElementException}. This method takes time linear in the number of
     * observed values.
     *
     * @param p Must be in [0.0, 1.0)
     */
    public Optional<Double> percentile(double p) {
      checkArgument(p >= 0.0 && p < 1.0, "Percentiles must be in [0.0, 1.0)");
      if (data.length == 0) {
        return Optional.absent();
      }
      return Optional.of(algorithm.computePercentile(p, data));
    }

    public List<Optional<Double>> percentiles(Iterable<Double> percentilesToGet) {
      final ImmutableList.Builder<Optional<Double>> ret = ImmutableList.builder();
      for (final double percentile : percentilesToGet) {
        ret.add(percentile(percentile));
      }
      return ret.build();
    }
  }
}
