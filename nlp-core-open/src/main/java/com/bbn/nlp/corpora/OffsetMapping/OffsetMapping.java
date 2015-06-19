package com.bbn.nlp.corpora.OffsetMapping;

import com.google.common.base.Optional;
import com.google.common.collect.Range;

import java.util.Collection;

/**
 * Contains a mapping between of item (typically character) indices from one numbering
 * scheme to another.
 *
 * Define an offset assignment (OA) for a sequence L as a set (U, f) where U is a set of
 * integers such that L_i is not assigned an offset and f is a function from S-U --> integers
 * such that f(L_i) <= f(L_j) for all i<=j.
 *
 * This class stores a correspondence between a source offset assignment and a target offset assignment.
 * A canonical example might be between character offsets and EDT offsets in ACE.
 *
 * See @{code
 * AbstractOffsetMappping} a for default implementation.
 */
interface OffsetMapping {

  /**
   * Maps an offset in the source to the corresponding offset or offsets (perhaps none) in the
   * target.  This is {f_b(x) | x \in f_a^-1(sourceIndex)}.
   *
   * If offsetIdx is out-of-bounds in the source, throws an {@link IndexOutOfBoundsException}.
   **/
  Collection<Integer> mapOffset(final int sourceIndex);

  /**
   * Maps a range of offsets in the source to the corresponding offset ranges in the target (if
   * any).   Note that a single source range may map to multiple target ranges (for example, if the
   * source is EDT offsets and we are mapping to character offsets).  This is equivalent to applying
   * {@link #mapOffset(int)} to each index in {@code closedSource} and then coalescing all
   * adjecent results into {@link Range}s.
   *
   * If the range is out-of-bounds in the source, throws an IndexOutOfBoundsException. If the source
   * range is not closed on both ends, throws an IllegalArgumentException. All returned Ranges will
   * be closed on both ends.
   *
   */
  Collection<Range<Integer>> mapRange(final Range<Integer> closedSource);

  /**
   * Returns a mapping from target to source, if possible.
   */
  Optional<OffsetMapping> inverseMapping();

  /**
   * is this source index mapped?
   * @param source
   * @return
   */
  boolean isMapped(final int source);

  /**
   * Returns true iff every index in the source range has a mapping in the target.
   * {@code source} must be a closed interval or an {@link IllegalArgumentException}
   * will be thrown.
   */
  boolean isEntirelyMapped(final Range<Integer> closedSource);
}
