package com.bbn.nlp.coreference.measures;

import com.bbn.bue.common.evaluation.FMeasureInfo;

import com.google.common.annotations.Beta;

@Beta
public interface CEAFScorer {

  public FMeasureInfo score(final Iterable<? extends Iterable<?>> predicted, final Iterable<? extends Iterable<?>> gold);
}
