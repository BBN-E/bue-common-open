package com.bbn.bue.common.evaluation;

import com.bbn.bue.common.StringUtils;
import com.bbn.bue.common.primitives.DoubleUtils;
import com.bbn.bue.common.symbols.Symbol;
import com.bbn.bue.common.symbols.SymbolUtils;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.bbn.bue.common.primitives.DoubleUtils.IsNonNegative;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.all;

/**
 * Utilities for working with {@link SummaryConfusionMatrix}es.  In particular, to build a
 * {@link SummaryConfusionMatrix}, use {@link  #builder()}.
 *
 * Other useful things: computing F-measures ({@link #FMeasureVsAllOthers(SummaryConfusionMatrix, Symbol)})
 * and pretty-printing ({@link #prettyPrint(SummaryConfusionMatrix)}.
 *
 * @author rgabbard
 */
public final class SummaryConfusionMatrices {

  private SummaryConfusionMatrices() {
    throw new UnsupportedOperationException();
  }

  public static String prettyPrint(SummaryConfusionMatrix m, Ordering<Symbol> labelOrdering) {
    final StringBuilder sb = new StringBuilder();

    for (final Symbol key1 : labelOrdering.sortedCopy(m.leftLabels())) {
      for (final Symbol key2 : labelOrdering.sortedCopy(m.rightLabels())) {
        sb.append(String.format("%s / %s: %6.2f\n", key1, key2, m.cell(key1, key2)));
      }
    }

    return sb.toString();
  }

  public static String prettyDelimPrint(final SummaryConfusionMatrix m, final String delimiter) {
    return prettyDelimPrint(m, delimiter, SymbolUtils.byStringOrdering());
  }

  public static String prettyDelimPrint(final SummaryConfusionMatrix m,
      final String delimiter, final Ordering<Symbol> labelOrdering) {
    final Joiner delimJoiner = Joiner.on(delimiter);
    final ImmutableList.Builder<String> lines = ImmutableList.builder();

    final List<Symbol> rowLabels = labelOrdering.sortedCopy(m.leftLabels());
    final List<Symbol> columnLabels = labelOrdering.sortedCopy(m.rightLabels());

    // Create header
    final ImmutableList.Builder<String> header = ImmutableList.builder();
    header.add("Predicted");
    header.addAll(Iterables.transform(columnLabels, SymbolUtils.desymbolizeFunction()));
    lines.add(delimJoiner.join(header.build()));

    // Output each line
    for (final Symbol rowLabel : rowLabels) {
      final ImmutableList.Builder<String> row = ImmutableList.builder();
      row.add(rowLabel.asString());
      for (final Symbol columnLabel : columnLabels) {
        row.add(String.format("%.2f", m.cell(rowLabel, columnLabel)));
      }
      lines.add(delimJoiner.join(row.build()));
    }

    // Return all lines
    return StringUtils.NewlineJoiner.join(lines.build());
  }

  public static String prettyPrint(SummaryConfusionMatrix m) {
    return prettyPrint(m, SymbolUtils.byStringOrdering());
  }

  public static final FMeasureCounts FMeasureVsAllOthers(SummaryConfusionMatrix m,
      final Symbol positiveSymbol) {
    return FMeasureVsAllOthers(m, ImmutableSet.of(positiveSymbol));
  }

  public static final FMeasureCounts FMeasureVsAllOthers(SummaryConfusionMatrix m,
      final Set<Symbol> positiveSymbols) {
    double truePositives = 0;

    for (final Symbol goodSymbol : positiveSymbols) {
      for (final Symbol goodSymbol2 : positiveSymbols) {
        truePositives += m.cell(goodSymbol, goodSymbol2);
      }
    }

    double falsePositives = -truePositives;
    double falseNegatives = -truePositives;

    for (final Symbol goodSymbol : positiveSymbols) {
      falsePositives += m.rowSum(goodSymbol);
      falseNegatives += m.columnSum(goodSymbol);
    }

    return FMeasureCounts.from((float) truePositives, (float) falsePositives,
        (float) falseNegatives);
  }


  /**
   * Returns accuracy, which is defined as the sum of the cells of the form (X,X) over the sum of
   * all cells.  If the sum is 0, 0 is returned.  To pretty-print this you probably want to multiply
   * by 100.
   */
  public static final double accuracy(SummaryConfusionMatrix m) {
    final double total = m.sumOfallCells();
    double matching = 0.0;
    for (final Symbol key : Sets.intersection(m.leftLabels(), m.rightLabels())) {
      matching += m.cell(key, key);
    }
    if (total != 0.0) {
      return matching / total;
    } else {
      return 0.0;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * To build a {@link SummaryConfusionMatrix}, call {@link SummaryConfusionMatrices#builder()}. On
   * the returned object, call {@link #accumulatePredictedGold(Symbol, Symbol, double)} to record
   * the number of times a system response corresponds to a gold standard responses for some item.
   * Typically the double value will be 1.0 unless you are using fractional counts for some reason.
   *
   * When done, call {@link #build()} to get a {@link SummaryConfusionMatrix}.
   */
  public static class Builder {

    private final Table<Symbol, Symbol, Double> table = HashBasedTable.create();

    public Builder accumulate(final SummaryConfusionMatrix matrix) {
      matrix.accumulateTo(this);
      return this;
    }

    public Builder accumulate(final Symbol row, final Symbol col, final double val) {
      final Double cur = table.get(row, col);
      final double setVal;
      if (cur != null) {
        setVal = cur + val;
      } else {
        setVal = val;
      }
      table.put(row, col, setVal);
      return this;
    }

    /**
     * This is just an alias for accumulate. However, since the F-measure functions assume the
     * predictions are on the rows and the gold-standard on the columns, using this method in such
     * cases and make the code clearer and reduce errors.
     */
    public Builder accumulatePredictedGold(final Symbol prediction, final Symbol gold,
        final double val) {
      accumulate(prediction, gold, val);
      return this;
    }

    public SummaryConfusionMatrix build() {
      // first attemtp the more efficient implementation for the common binary case
      final Optional<BinarySummaryConfusionMatrix> binaryImp =
          BinarySummaryConfusionMatrix.attemptCreate(table);
      if (binaryImp.isPresent()) {
        return binaryImp.get();
      } else {
        return new TableBasedSummaryConfusionMatrix(table);
      }
    }

    public static final Function<Builder, SummaryConfusionMatrix> Build =
        new Function<Builder, SummaryConfusionMatrix>() {
          @Override
          public SummaryConfusionMatrix apply(Builder input) {
            return input.build();
          }
        };

    private Builder() {
    }
  }
}

// here be implementation details users don't need to be concerned with

class TableBasedSummaryConfusionMatrix implements SummaryConfusionMatrix {

  private final Table<Symbol, Symbol, Double> table;

  public double cell(final Symbol row, final Symbol col) {
    final Double ret = table.get(row, col);
    if (ret != null) {
      return ret;
    } else {
      return 0.0;
    }
  }

  /**
   * The left-hand labels of the confusion matrix.
   */
  public Set<Symbol> leftLabels() {
    return table.rowKeySet();
  }

  /**
   * The right hand labels of the confusion matrix.
   */
  public Set<Symbol> rightLabels() {
    return table.columnKeySet();
  }


  TableBasedSummaryConfusionMatrix(final Table<Symbol, Symbol, Double> table) {
    this.table = ImmutableTable.copyOf(table);
    checkArgument(all(table.values(), IsNonNegative));
  }

  public double sumOfallCells() {
    return DoubleUtils.sum(table.values());
  }

  public double rowSum(Symbol rowSymbol) {
    return DoubleUtils.sum(table.row(rowSymbol).values());
  }

  public double columnSum(Symbol columnSymbol) {
    return DoubleUtils.sum(table.column(columnSymbol).values());
  }

  @Override
  public SummaryConfusionMatrix filteredCopy(CellFilter filter) {
    final SummaryConfusionMatrices.Builder ret = SummaryConfusionMatrices.builder();
    for (final Table.Cell<Symbol, Symbol, Double> cell : table.cellSet()) {
      if (filter.keepCell(cell.getRowKey(), cell.getColumnKey())) {
        ret.accumulate(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
      }
    }
    return ret.build();
  }

  @Override
  public SummaryConfusionMatrix copyWithTransformedLabels(Function<Symbol, Symbol> f) {
    final SummaryConfusionMatrices.Builder ret = SummaryConfusionMatrices.builder();
    for (final Table.Cell<Symbol, Symbol, Double> cell : table.cellSet()) {
      ret.accumulate(f.apply(cell.getRowKey()), f.apply(cell.getColumnKey()), cell.getValue());
    }
    return ret.build();
  }

  public void accumulateTo(SummaryConfusionMatrices.Builder builder) {
    for (final Table.Cell<Symbol, Symbol, Double> cell : table.cellSet()) {
      builder.accumulate(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
    }
  }
}

/**
 * The special case where there are only two labels is very common, so we provide a much more
 * efficient implementation for it.  This makes a noticeable difference when e.g. doing bootstrap
 * sampling with many different score breakdowns.
 */
class BinarySummaryConfusionMatrix implements SummaryConfusionMatrix {

  private final Symbol key0;
  private final Symbol key1;
  private final double[] data;

  private static final int NOT_PRESENT = -1;

  BinarySummaryConfusionMatrix(Symbol key0, Symbol key1, double[] data) {
    checkArgument(key0 != key1);
    checkArgument(data.length == 4);
    this.key0 = checkNotNull(key0);
    this.key1 = checkNotNull(key1);
    // no defensive copy because we control where this comes from
    this.data = checkNotNull(data);
  }

  public static boolean canUseFor(Table<Symbol, Symbol, Double> table) {
    return table.rowKeySet().size() == 2 &&
        table.rowKeySet().equals(table.columnKeySet());
  }

  public static Optional<BinarySummaryConfusionMatrix> attemptCreate(
      Table<Symbol, Symbol, Double> table) {
    if (canUseFor(table)) {
      final Iterator<Symbol> keyIt = table.rowKeySet().iterator();
      final Symbol key0 = keyIt.next();
      final Symbol key1 = keyIt.next();
      return Optional.of(new BinarySummaryConfusionMatrix(key0, key1,
          new double[]{cell(table, key0, key0), cell(table, key0, key1),
              cell(table, key1, key0), cell(table, key1, key1)}));
    } else {
      return Optional.absent();
    }
  }

  private static double cell(Table<Symbol, Symbol, Double> table, Symbol row, Symbol col) {
    final Double val = table.get(row, col);
    if (val != null) {
      return val;
    } else {
      return 0.0;
    }
  }

  @Override
  public double cell(Symbol row, Symbol col) {
    int rowIdx = keyIndex(row);
    int colIdx = keyIndex(col);
    if (rowIdx == NOT_PRESENT || colIdx == NOT_PRESENT) {
      return 0.0;
    }
    return data[2 * rowIdx + colIdx];
  }

  public void accumulateTo(SummaryConfusionMatrices.Builder builder) {
    builder.accumulate(key0, key0, data[0]);
    builder.accumulate(key0, key1, data[1]);
    builder.accumulate(key1, key0, data[2]);
    builder.accumulate(key1, key1, data[3]);
  }

  private int keyIndex(Symbol sym) {
    if (sym == key0) {
      return 0;
    } else if (sym == key1) {
      return 1;
    } else {
      return NOT_PRESENT;
    }
  }

  @Override
  public Set<Symbol> leftLabels() {
    return ImmutableSet.of(key0, key1);
  }

  @Override
  public Set<Symbol> rightLabels() {
    return ImmutableSet.of(key0, key1);
  }

  @Override
  public double sumOfallCells() {
    return DoubleUtils.sum(data);
  }

  @Override
  public double rowSum(Symbol row) {
    int rowIdx = keyIndex(row);
    if (NOT_PRESENT == rowIdx) {
      return 0.0;
    }
    return data[2 * rowIdx] + data[2 * rowIdx + 1];
  }

  @Override
  public double columnSum(Symbol column) {
    int colIdx = keyIndex(column);
    if (NOT_PRESENT == colIdx) {
      return 0.0;
    }
    return data[colIdx] + data[colIdx + 2];
  }

  @Override
  public SummaryConfusionMatrix filteredCopy(CellFilter filter) {
    final SummaryConfusionMatrices.Builder builder = SummaryConfusionMatrices.builder();
    for (final Symbol left : leftLabels()) {
      for (final Symbol right : rightLabels()) {
        if (filter.keepCell(left, right)) {
          builder.accumulate(left, right, cell(left, right));
        }
      }
    }
    return builder.build();
  }

  @Override
  public SummaryConfusionMatrix copyWithTransformedLabels(Function<Symbol, Symbol> f) {
    final SummaryConfusionMatrices.Builder builder = SummaryConfusionMatrices.builder();
    for (final Symbol left : leftLabels()) {
      for (final Symbol right : rightLabels()) {
        builder.accumulate(f.apply(left), f.apply(right), cell(left, right));
      }
    }
    return builder.build();
  }

}

