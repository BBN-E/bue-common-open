package com.bbn.bue.common.math;

/**
 * Utilities methods for working with probabilities.
 *
 * @author Ryan Gabbard
 */
public final class ProbabilityUtils {

  private ProbabilityUtils() {
    throw new UnsupportedOperationException();
  }


  public static final double DEFAULT_DOUBLE_EPSILON = .000001;
  public static final float DEFAULT_FLOAT_EPSILON = .000001f;

  /**
   * Calls {@link #cleanProbability(double, double, boolean)} with a default epsilon value
   * of .000001.
   */
  public static double cleanProbability(double prob, boolean allowOne) {
    return cleanProbability(prob, DEFAULT_DOUBLE_EPSILON, allowOne);
  }

  /**
   * Cleans up input which should be probabilities. Occasionally due to numerical stability issues
   * you get input which should be a probability but could actually be very slightly less than 0 or
   * more than 1.0.  This function will take values within epsilon of being good probabilities and
   * fix them. If the prob is within epsilon of zero, it is changed to +epsilon. One the upper end,
   * if allowOne is true, anything between 1.0 and 1.0 + epsilon is mapped to 1.0. If allowOne is
   * false, anything between 1.0-epsilon and 1.0 + epsilon is mapped to 1.0-epsilon.  All other
   * probability values throw an unchecked InvalidProbabilityException.
   */
  public static double cleanProbability(double prob, double epsilon, boolean allowOne) {
    if (prob < -epsilon || prob > (1.0 + epsilon)) {
      throw new InvalidProbabilityException(prob);
    }

    if (prob < epsilon) {
      prob = epsilon;
    } else {
      final double limit = allowOne ? 1.0 : (1.0 - epsilon);
      if (prob > limit) {
        prob = limit;
      }
    }

    return prob;
  }

  /**
   * {@code float} version of {@link #cleanProbability(double, boolean)}
   */
  public static float cleanProbability(float prob, boolean allowOne) {
    return cleanProbability(prob, DEFAULT_FLOAT_EPSILON, allowOne);
  }

  /**
   * {@code float} version of {@link #cleanProbability(double, double, boolean)}
   */
  public static float cleanProbability(float prob, float epsilon, boolean allowOne) {
    if (prob < -epsilon || prob > (1.0 + epsilon)) {
      throw new InvalidProbabilityException(prob);
    }

    if (prob < epsilon) {
      prob = epsilon;
    } else {
      final float limit = allowOne ? 1.0f : (1.0f - epsilon);
      if (prob > limit) {
        prob = limit;
      }
    }

    return prob;
  }

  public static class InvalidProbabilityException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidProbabilityException(double d) {
      super(String.format("Invalid probability %s", d));
    }

    public InvalidProbabilityException(float f) {
      super(String.format("Invalid probability %s", f));
    }
  }
}
