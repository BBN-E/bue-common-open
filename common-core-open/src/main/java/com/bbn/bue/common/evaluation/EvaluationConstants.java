package com.bbn.bue.common.evaluation;

import com.bbn.bue.common.symbols.Symbol;

/**
 * Convenience constants for using in evaluation code.
 * When building confusion matrices for binary decisions, we use these conventional constants as
 * the labels.
 */
public final class EvaluationConstants {
  public static final Symbol PRESENT = Symbol.from("Present");
  public static final Symbol ABSENT = Symbol.from("Absent");
}
