package com.bbn.bue.common.evaluation;

/**
 * Something which can produce an {@link Alignment}. See that class for more details.
 */
public interface Aligner<LeftT, RightT> {

  Alignment<LeftT, RightT> align(Iterable<? extends LeftT> leftItems,
      Iterable<? extends RightT> rightItems);
}
