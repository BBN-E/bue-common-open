package com.bbn.bue.common.strings.offsets;

import com.google.common.base.Optional;

/**
 * An {@link OffsetMapping} where each source index is mapped to guaranteed to map to at most one
 * target index.
 */
interface FunctionalOffsetMapping extends OffsetMapping {

  /**
   * Maps an offset in the source to the corresponding offset  in the target, which is guaranteed to
   * be unique.  This is {@code f_b(f_a^-1(sourceIndex))}.
   *
   * If offsetIdx is out-of-bounds in the source, throws an {@link IndexOutOfBoundsException}.
   */
  Optional<Integer> mapOffsetUniquely(int sourceIdx);
}
