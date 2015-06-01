package com.bbn.nlp.coreference.measures;

import com.google.common.annotations.Beta;

/**
 * Scores coreference clusterings by the BLANC measure. See Marta Recasens Potau.  "Coreference:
 * Theory, Annotation, Resolution, and Evaluation." PhD. Dissertation. University of Barcelona
 * http://stel.ub.edu/cba2010/phd/phd.pdf
 */
@Beta
public interface BLANCScorer {

  /**
   * Scores the provided {@code predicted} clustering against the {@code gold} clustering
   * using some variant of the BLANC metric.  If the score is undefined, {@link com.google.common.base.Optional#absent()}
   * is returned.
   * @param predicted
   * @param gold
   * @return
   */
  public BLANCResult score(final Iterable<? extends Iterable<?>> predicted,
      final Iterable<? extends Iterable<?>> gold);
}
