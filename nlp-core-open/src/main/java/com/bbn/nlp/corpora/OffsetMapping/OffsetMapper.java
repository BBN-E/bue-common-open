package com.bbn.nlp.corpora.OffsetMapping;

import com.google.common.collect.Range;

import java.util.Collection;

/**
 * Offset Mapper - handling of character encoding is left to implementations. See @{code
 * AbstractOffsetMapper} for default implementations
 *
 * It is up to implementations to ensure that all ranges passed are of the form [closed, closed]
 */
interface OffsetMapper {

  /**
   * Maps an offset in the source to the corresponding offset or offsets (perhaps none) in the
   * target.  If offsetIdx is out-of-bounds in the source, throws an IndexOutOfBoundsException.
   **/
  Collection<Integer> mapOffset(final int offsetIdx);

  /**
   * Maps a range of offsets in the source to the corresponding offset ranges in the target (if
   * any).   Note that a single source range may map to multiple target ranges (for example, if the
   * source is EDT offsets and we are mapping to character offsets).
   *
   * If the range is out-of-bounds in the source, throws an IndexOutOfBoundsException. If the source
   * range is not closed on both ends, throws an IllegalArgumentException. All returned Ranges will
   * be closed on both ends.
   *
   * @param closedSource - a range that is closed on the left, open on the right, typically
   *                         constructed as Range.closed(offset, offset + length-1)
   */
  Collection<Range<Integer>> mapRange(final Range<Integer> closedSource);

  /**
   * makes a best effort to return a reverse mapping
   */
  OffsetMapper inverseMapper();

  /**
   * is this source index mapped?
   * @param source
   * @return
   */
  boolean isMapped(final int source);

  /**
   * is everything in [closed, closed] mapped?
   * @param closedSource
   * @return
   */
  boolean isEntirelyMapped(final Range<Integer> closedSource);
}
