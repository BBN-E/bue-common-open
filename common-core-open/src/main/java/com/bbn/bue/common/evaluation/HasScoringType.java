package com.bbn.bue.common.evaluation;

import com.bbn.bue.common.symbols.Symbol;
import com.google.common.annotations.Beta;

/**
 * Anything that has an associated {@link Symbol} type that can be used for the purposes of scoring.
 */
@Beta
public interface HasScoringType {
  Symbol scoringType();
}
