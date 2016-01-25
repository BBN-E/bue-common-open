package com.bbn.bue.common;

/**
 * An object which can accumulate objects of type {@code T} and return some sort of result
 * of the accumulation which is of the same type.
 *
 * This is inspired by FACTORIE's Accumulator class.
 * @param <T>
 */
public interface Accumulator<T> {

  /**
   * Adds the provided data to the accumulator.
   */
  void accumulate(T data);

  /**
   * Adds all the data previously accumulated to the provided accumulator to this one.
   * @param accumulator
   */
  void accumulate(Accumulator<T> accumulator);

  /**
   * Get the current 'sum' for this accumulator.
   * @return
   */
  T value();

  void reset();
}

