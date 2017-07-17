package com.bbn.bue.common.collections;

import com.bbn.bue.common.TextGroupImmutable;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import org.immutables.value.Value;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Multitable that can hold duplicate key-key-value triplets and that maintains the insertion
 * ordering of values for a given key-key-value triplet. See {@link Multitable} and {@link
 * ImmutableMultitable} documentation for information common to all multitables.
 *
 * Row and column iteration order is based on the order of the first insertion of row or column
 * keys. Cell iteration order is row-major, first following the iteration order of rows then
 * columns.
 */
public final class ImmutableListMultitable<R, C, V> extends ImmutableMultitable<R, C, V>
    implements ListMultitable<R, C, V> {


  private final ImmutableTable<R, C, Collection<V>> table;
  private final int size;
  private final ImmutableMap<R, Multimap<C, V>> rowMap;
  private final ImmutableMap<C, Multimap<R, V>> columnMap;
  private final ImmutableSet<Multicell<R, C, V>> cellSet;
  private final ImmutableList<V> allValues;

  private ImmutableListMultitable(final ImmutableTable<R, C, Collection<V>> table,
      final int size, final ImmutableSet<R> rowIterationOrder,
      final ImmutableSet<C> columnIterationOrder) {

    this.table = checkNotNull(table);
    this.size = size;     //all available construction methods ensure size matches table

    //This is more than we generally want in constructor, but we are only caching to return views.
    //cache rowMap
    final ImmutableMap.Builder<R, Multimap<C, V>> rowMapBuilder = ImmutableMap.builder();
    for (final R rowKey : rowIterationOrder) {
      final ImmutableListMultimap.Builder<C, V> colMultiBuilder = ImmutableListMultimap.builder();
      for (final C columnKey : columnIterationOrder) {
        final Collection<V> value = table.get(rowKey, columnKey);
        if (value != null) {
          colMultiBuilder.putAll(columnKey, value);
        }
      }
      rowMapBuilder.put(rowKey, colMultiBuilder.build());
    }
    this.rowMap = rowMapBuilder.build();
    //cache columnMap
    final ImmutableMap.Builder<C, Multimap<R, V>> columnMapBuilder = ImmutableMap.builder();
    for (final C columnKey : columnIterationOrder) {
      final ImmutableListMultimap.Builder<R, V> rowMultiBuilder = ImmutableListMultimap.builder();
      for (final R rowKey : rowIterationOrder) {
        final Collection<V> value = table.get(rowKey, columnKey);
        if (value != null) {
          rowMultiBuilder.putAll(rowKey, value);
        }
      }
      columnMapBuilder.put(columnKey, rowMultiBuilder.build());
    }
    this.columnMap = columnMapBuilder.build();

    //cache cellSet and values
    final ImmutableList.Builder<V> valuesBuilder = ImmutableList.builder();
    final ImmutableSet.Builder<Multicell<R, C, V>> cellSetBuilder = ImmutableSet.builder();
    for (final R rowKey : rowIterationOrder) {
      for (final C columnKey : columnIterationOrder) {
        final Collection<V> values = table.get(rowKey, columnKey);
        if (values != null) {
        final ImmutableListMulticell.Builder<R, C, V> multicellBuilder =
            new ImmutableListMultitable.ListMulticell.Builder<R, C, V>()
                .rowKey(rowKey)
                .columnKey(columnKey);
          multicellBuilder.values(values);
          valuesBuilder.addAll(values);
          cellSetBuilder.add(multicellBuilder.build());
        }
      }
    }
    this.cellSet = cellSetBuilder.build();
    this.allValues = valuesBuilder.build();
  }

  @Override
  protected Table<R, C, Collection<V>> table() {
    return table;
  }

  /**
   * Returns the list of values corresponding to the given row and column keys, or
   * an empty set if no such mapping exists. This exists in addition to {@code get} as a type-safe
   * way of returning a list of values at a specified cell.
   *
   * @param rowKey    key of row to search for
   * @param columnKey key of column to search for
   */
  @Override
  @SuppressWarnings("unchecked")
  public ImmutableList<V> getAsList(@Nullable final Object rowKey,
      @Nullable final Object columnKey) {
    final Collection<V> ret = table.get(rowKey, columnKey);
    if (ret != null) {
      return (ImmutableList<V>) ret;  //cast guaranteed to succeed because table made of lists
    } else {
      return ImmutableList.of();
    }
  }

  @Override
  public ImmutableMap<R, Multimap<C, V>> rowMap() {
    return rowMap;
  }

  @Override
  public ImmutableMap<C, Multimap<R, V>> columnMap() {
    return columnMap;
  }

  @Override
  public Set<Multicell<R, C, V>> cellSet() {
    return cellSet;
  }

  @Override
  public Collection<V> get(@Nullable final Object rowKey, @Nullable final Object columnKey) {
    final Collection<V> ret = table().get(rowKey, columnKey);
    if (ret != null) {
      return ret;
    } else {
      return ImmutableList.of();
    }
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public ImmutableList<V> values() {
    return allValues;
  }

  public static <R, C, V> ImmutableListMultitable.Builder<R, C, V> builder() {
    return new Builder<>();
  }


  public static class Builder<R, C, V> implements ImmutableMultitable.Builder<R,C,V> {

    // we use the two tables because we both need to maintain insertion order and
    // be able to do lookups during building. The values of both sets are
    // the same identical objects
    private final HashBasedTable<R, C, ImmutableList.Builder<V>> tableWeCanLookUpIn =
        HashBasedTable.create();
    private final ImmutableSet.Builder<RowKeyColumnKeyPair<R, C>> rowInsertionOrder =
        ImmutableSet.builder();

    private Builder() {
    }

    private ImmutableList.Builder<V> setForKey(final R rowKey, final C columnKey) {
      ImmutableList.Builder<V> values = tableWeCanLookUpIn.get(rowKey, columnKey);
      if (values == null) {
        values = ImmutableList.builder();
        tableWeCanLookUpIn.put(rowKey, columnKey, values);
        rowInsertionOrder.add(RowKeyColumnKeyPair.of(rowKey, columnKey));
      }
      return values;
    }

    @Override
    public Builder<R, C, V> put(R rowKey, C columnKey, V value) {
      setForKey(rowKey, columnKey).add(value);
      return this;
    }

    @Override
    public Builder<R, C, V> putAll(final R rowKey, final C columnKey,
        final Iterable<? extends V> values) {
      setForKey(rowKey, columnKey).addAll(values);
      return this;
    }

    public ImmutableListMultitable<R, C, V> build() {
      final ImmutableTable.Builder<R, C, Collection<V>> immutableTable =
          ImmutableTable.builder();

      int size = 0;
      ImmutableSet.Builder<R> rowIterationBuilder = ImmutableSet.builder();
      ImmutableSet.Builder<C> columnIterationBuilder = ImmutableSet.builder();

      for (final RowKeyColumnKeyPair<R, C> rowKeyColKey : rowInsertionOrder.build()) {
        final ImmutableList<V> valuesForPair =
            tableWeCanLookUpIn.get(rowKeyColKey.row(), rowKeyColKey.column()).build();
        size += valuesForPair.size();
        immutableTable.put(rowKeyColKey.row(), rowKeyColKey.column(),
            valuesForPair);
        rowIterationBuilder.add(rowKeyColKey.row());
        columnIterationBuilder.add(rowKeyColKey.column());
      }

      return new ImmutableListMultitable<>(immutableTable.build(), size,
          rowIterationBuilder.build(),
          columnIterationBuilder.build());
    }
  }

  @TextGroupImmutable
  @Value.Immutable
  static abstract class ListMulticell<R, C, V>
      implements ListMultitable.ListMulticell<R, C, V> {

    @Override
    public abstract R getRowKey();

    @Override
    public abstract C getColumnKey();

    @Override
    public abstract List<V> getValues();

    public static class Builder<R, C, V> extends ImmutableListMulticell.Builder<R, C, V> {

    }
  }
}

