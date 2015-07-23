package com.bbn.bue.common.evaluation;

import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Function;

import java.util.Set;

public interface SummaryConfusionMatrix {

  double cell(final Symbol row, final Symbol col);
  /**
   * The left-hand labels of the confusion matrix.
   */
  Set<Symbol> leftLabels();
  /**
   * The right hand labels of the confusion matrix.
   */
  Set<Symbol> rightLabels();

  double sumOfallCells();

  double rowSum(Symbol row);

  double columnSum(Symbol column);

  SummaryConfusionMatrix filteredCopy(CellFilter filter);

  SummaryConfusionMatrix copyWithTransformedLabels(Function<Symbol, Symbol> f);

  void accumulateTo(SummaryConfusionMatrices.Builder builder);

  interface CellFilter {
    boolean keepCell(Symbol row, Symbol column);
  }
}
