package com.bbn.bue.common;

/**
 * An {@link Accumulator} which can accumulate fractionally (e.g. add half an item).
 * @param <T>
 */
public interface ScalableAccumulator<T> extends Accumulator<T> {
  /**
   * Adds the provided data to the accumulator, scaled by the provided factor.
   */
  void accumulate(T data, double factor);

  /**
   * Adds all the data previously accumulated to the provided accumulator to this one, scaled
   * by the provided factor.
   * @param accumulator
   */
  void accumulate(Accumulator<T> accumulator, double factor);
}
