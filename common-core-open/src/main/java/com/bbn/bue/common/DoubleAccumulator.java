package com.bbn.bue.common;

/**
 * An {@link Accumulator} for {@code double}s.
 *
 * Based on FACTORIE's {@code DoubleAccumulator}.
 */
public final class DoubleAccumulator implements ScalableAccumulator<Double> {
  private double val = 0.0;

  private DoubleAccumulator() {

  }

  public static DoubleAccumulator create() {
    return new DoubleAccumulator();
  }

  @Override
  public void accumulate(final Double x) {
    val+=x;
  }

  @Override
  public void accumulate(final Double data, final double factor) {
    val += data * factor;
  }

  @Override
  public void accumulate(final Accumulator<Double> accumulator) {
    val+=accumulator.value();
  }

  @Override
  public void accumulate(final Accumulator<Double> accumulator, double factor) {
    val+=accumulator.value()*factor;
  }


  @Override
  public void reset() {
    val = 0.0;
  }

  @Override
  public Double value() {
    return val;
  }

  @Override
  public String toString() {
    return "accumulator=" + val;
  }
}
