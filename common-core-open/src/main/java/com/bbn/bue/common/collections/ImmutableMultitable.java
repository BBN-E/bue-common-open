package com.bbn.bue.common.collections;

import com.bbn.bue.common.TextGroupImmutable;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import org.immutables.value.Value;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * An immutable {@link Multitable}.
 *
 * Guava immutable collections generally guarantee that the iteration order is the same as the
 * insertion order. In a multiable, for a single pair of row and column indices, multiple insertions
 * may occur and the handling of duplicates is specified by the implementation, so it is difficult
 * to faithfully recreate the insertion order for iteration. To partially reflect the insertion
 * order, iteration over rows and columns reflects the order in which values were first inserted
 * into those rows and columns. Cell iteration order is row-major, first following the iteration
 * order of rows then of columns.
 *
 * @author Chester Palen-Michel, Constantine Lignos, Ryan Gabbard
 */
public abstract class ImmutableMultitable<R, C, V> extends AbstractMultitable<R,C,V> implements Multitable<R, C, V> {

  @Override
  public boolean contains(@Nullable final Object rowKey, @Nullable final Object columnKey) {
    return table().contains(rowKey, columnKey);
  }

  @Override
  public boolean containsRow(@Nullable final Object rowKey) {
    return table().containsRow(rowKey);
  }

  @Override
  public boolean containsColumn(@Nullable final Object columnKey) {
    return table().containsColumn(columnKey);
  }

  @Override
  public boolean containsValue(@Nullable final Object value) {
    for (final R rowKey : table().rowKeySet()) {
      for (final C columnKey : table().columnKeySet()) {
       final Collection<V> values = table().get(rowKey, columnKey);
        if (values != null) {
          // The argument must always be of type Object by contract, even though the collection is
          // of type V
          //noinspection SuspiciousMethodCalls
          if (values.contains(value)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public boolean isEmpty() {
    return table().isEmpty();
  }

  @Override
  public Set<R> rowKeySet() {
    return table().rowKeySet();
  }

  @Override
  public Set<C> columnKeySet() {
    return table().columnKeySet();
  }

  @Override
  public Multimap<C, V> row(final R rowKey) {
    final Multimap<C, V> row = rowMap().get(rowKey);
    if (row != null){
      return row;
    } else {
      return ImmutableMultimap.of();
    }
  }

  @Override
  public Multimap<R, V> column(final C columnKey) {
    final Multimap<R, V> col = columnMap().get(columnKey);
    if (col != null){
      return col;
    } else {
      return ImmutableMultimap.of();
    }
  }

  /**
   * Guaranteed to throw an exception and leave the table unmodified.
   *
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public final void clear() {
    throw new UnsupportedOperationException();
  }

  /**
   * Guaranteed to throw an exception and leave the table unmodified.
   *
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public boolean put(final R rowKey, final C columnKey, final V value) {
    throw new UnsupportedOperationException();
  }

  /**
   * Guaranteed to throw an exception and leave the table unmodified.
   *
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public boolean putAll(final R rowKey, final C columnKey, final Iterable<? extends V> values) {
    throw new UnsupportedOperationException();
  }

  /**
   * Guaranteed to throw an exception and leave the table unmodified.
   *
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public boolean putAll(final Multitable<? extends R, ? extends C, ? extends V> table) {
    throw new UnsupportedOperationException();
  }

  /**
   * Guaranteed to throw an exception and leave the table unmodified.
   *
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public boolean remove(@Nullable final Object rowKey, @Nullable final Object columnKey,
      final Object value) {
    throw new UnsupportedOperationException();
  }

  /**
   * Guaranteed to throw an exception and leave the table unmodified.
   *
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public boolean removeAll(@Nullable final Object rowKey, @Nullable final Object columnKey) {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the backing table.
   */
  protected abstract Table<R, C, Collection<V>> table();

  /**
   * For use in {@code ImmutableMultitable} builders only.
   */
  @TextGroupImmutable
  @Value.Immutable
  static abstract class RowKeyColumnKeyPair<R, C> {

    public abstract R row();

    public abstract C column();

    public static <R, C> RowKeyColumnKeyPair<R, C> of(R row, C col) {
      return ImmutableRowKeyColumnKeyPair.<R, C>builder().row(row).column(col).build();
    }
  }

  public interface Builder<R,C,V> {
    ImmutableMultitable.Builder<R, C, V> put(R rowKey, C columnKey, V value);
    ImmutableMultitable.Builder<R, C, V> putAll(final R rowKey, final C columnKey,
        final Iterable<? extends V> values);
  }
}

