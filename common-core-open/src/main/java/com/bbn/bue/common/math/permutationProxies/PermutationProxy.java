package com.bbn.bue.common.math.permutationProxies;

/**
 * Provides the data-type-specific code necessary to permute arbitrary sorts of sequences.  For
 * example, you could (and we do) provide a permutation proxy which knows how to permute the rows of
 * a matrix. A separate PermutationProxy instance should be created for each sequence being
 * permuted.
 *
 * @author Ryan Gabbard
 */
public interface PermutationProxy {

  /**
   * Must store the data at position {@code srcIdx} in a single-element buffer.
   */
  void shiftIntoTemporaryBufferFrom(int srcIdx);

  /**
   * Set the data at position {@code destIdx} to the last value assigned to the buffer.
   */
  void shiftOutOfTemporaryBufferTo(int destIdx);

  /**
   * Must copy the data currently at position {@code srcIdx} to position {@code destIdx}.  Supplied
   * indices are guaranteed to be valid.
   */
  void shift(int srcIdx, int destIdx);

  /**
   * The number of items in the sequence.
   */
  int length();
}
