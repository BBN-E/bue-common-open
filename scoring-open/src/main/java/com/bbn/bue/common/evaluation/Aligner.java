package com.bbn.bue.common.evaluation;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;

/**
 * Something which can produce an {@link Alignment}. See that class for more details.
 */
@Beta
public interface Aligner<LeftT, RightT> {

  Alignment<LeftT, RightT> align(Iterable<? extends LeftT> leftItems,
      Iterable<? extends RightT> rightItems);

  Function<EvalPair<? extends Iterable<? extends LeftT>, ? extends Iterable<? extends RightT>>,
      Alignment<LeftT, RightT>> asFunction();
}
