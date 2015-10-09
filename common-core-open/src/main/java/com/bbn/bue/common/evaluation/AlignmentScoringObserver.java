package com.bbn.bue.common.evaluation;

import com.bbn.bue.common.UnaryContextObserver;

import com.google.common.annotations.Beta;

/**
 * A {@link UnaryContextObserver} which scores over alignments.
 */
@Beta
public interface AlignmentScoringObserver<CtxT, LeftT, RightT>
    extends UnaryContextObserver<CtxT, Alignment<LeftT, RightT>> {

  void observe(CtxT context, Alignment<LeftT, RightT> alignment);
}
