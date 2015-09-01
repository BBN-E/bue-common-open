package com.bbn.bue.common.collections;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;

import java.util.Map;

/**
 * Created by jdeyoung on 5/27/15.
 */
@Beta
public class TableUtils {


  public static <R, C, V> ImmutableTable<R, C, V> tableFromIndexFunctions(final Iterable<V> values,
      final Function<V, R> rowIndexFunction, final Function<V, C> columnIndexFunction) {
    final ImmutableTable.Builder<R, C, V> builder = ImmutableTable.builder();
    // this must be done in an imperative style since the column or row (or both) index functions
    // are not necessarily unique
    for (V v : values) {
      builder.put(rowIndexFunction.apply(v), columnIndexFunction.apply(v), v);
    }
    return builder.build();
  }

  public static <R, C, V, C2> ImmutableTable<R, C2, V> columnTransformerByKeyOnly(
      final Table<R, C, V> table,
      final Function<C, C2> columnTransformer) {
    return columnTransformerByCell(table, new Function<Table.Cell<R, C, V>, C2>() {
      @Override
      public C2 apply(final Table.Cell<R, C, V> input) {
        return columnTransformer.apply(input.getColumnKey());
      }
    });
  }

  /**
   * columnTransformer has access to Key, Value information in each Table.Cell
   * @param table
   * @param columnTransformer
   * @param <R>
   * @param <C>
   * @param <V>
   * @param <C2>
   * @return
   */
  public static <R, C, V, C2> ImmutableTable<R, C2, V> columnTransformerByCell(
      final Table<R, C, V> table,
      final Function<Table.Cell<R, C, V>, C2> columnTransformer) {
    final ImmutableTable.Builder<R, C2, V> newTable = ImmutableTable.builder();
    for(Table.Cell<R, C, V> cell : table.cellSet()) {
      C2 col = columnTransformer.apply(cell);
      newTable.put(cell.getRowKey(), col, cell.getValue());
    }
    return newTable.build();
  }

  public static <R, C, V, R2> ImmutableTable<R2, C, V> rowTransformerByKeyOnly(
      final Table<R, C, V> table,
      final Function<R, R2> rowTransformer) {
    return rowTransformerByCell(table, new Function<Table.Cell<R, C, V>, R2>() {
      @Override
      public R2 apply(final Table.Cell<R, C, V> input) {
        return rowTransformer.apply(input.getRowKey());
      }
    });
  }

  public static <R, C, V, R2> ImmutableTable<R2, C, V> rowTransformerByCell(
      final Table<R, C,V> table, final Function<Table.Cell<R, C, V>, R2> rowTransformer) {
    final ImmutableTable.Builder<R2, C, V> newTable = ImmutableTable.builder();
    for(Table.Cell<R, C, V> cell : table.cellSet()) {
      R2 row = rowTransformer.apply(cell);
      newTable.put(row, cell.getColumnKey(), cell.getValue());
    }
    return newTable.build();
  }

  public static <R,C,V> void addColumnToBuilder(ImmutableTable.Builder<R,C,V> builder, R row, Map<C,V> column) {
    for(Map.Entry<C,V> e: column.entrySet()) {
      builder.put(row, e.getKey(), e.getValue());
    }
  }

  /**
   * Creates a new {@link ImmutableTable} from the provided {@link com.google.common.collect.Table.Cell}s.
   * The iteration order of the resulting table will respect the iteration order of the input cells.
   * Null keys and values are forbidden.
   */
  public static <R, C, V> ImmutableTable<R, C, V> copyOf(Iterable<Table.Cell<R, C, V>> cells) {
    final ImmutableTable.Builder<R, C, V> ret = ImmutableTable.builder();

    for (final Table.Cell<R, C, V> cell : cells) {
      ret.put(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
    }

    return ret.build();
  }

  /**
   * Guava {@link Function} to transform a cell to its row key.
   */
  public static <R, C, V> Function<Table.Cell<R, C, V>, R> toRowKeyFunction() {
    return new Function<Table.Cell<R, C, V>, R>() {
      @Override
      public R apply(final Table.Cell<R, C, V> input) {
        return input.getRowKey();
      }
    };
  }

  /**
   * Guava {@link Function} to transform a cell to its column key.
   */
  public static <R, C, V> Function<Table.Cell<R, C, V>, C> toColumnKeyFunction() {
    return new Function<Table.Cell<R, C, V>, C>() {
      @Override
      public C apply(final Table.Cell<R, C, V> input) {
        return input.getColumnKey();
      }
    };
  }
}
