package com.bbn.nlp.coreference.measures;

import com.bbn.bue.common.evaluation.FMeasureInfo;


public interface CEAFScorer {
  
  public FMeasureInfo score(final Iterable<? extends Iterable<?>> predicted, final Iterable<? extends Iterable<?>> gold);
}
