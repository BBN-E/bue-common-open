package com.bbn.bue.common.collections;

import com.google.common.base.Objects;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;


/**
 * A multitable combines the indexing semantics of a {@link Table} with the value semantics of a
 * {@link com.google.common.collect.Multimap}.
 *
 * Equality and hashcode for {@link Multitable}s is delegated to their {@link #cellSet()}s.
 *
 * Adapted from {@link Table}.
 *
 * @author Chester Palen-Michel, Constantine Lignos, Ryan Gabbard
 */
public interface Multitable<R, C, V> {
  // Accessors

  /**
   * Returns {@code true} if the table contains a mapping with the specified
   * row and column keys.
   *
   * @param rowKey    key of row to search for
   * @param columnKey key of column to search for
   */
  boolean contains(@Nullable Object rowKey, @Nullable Object columnKey);

  /**
   * Returns {@code true} if the table contains a mapping with the specified
   * row key.
   *
   * @param rowKey key of row to search for
   */
  boolean containsRow(@Nullable Object rowKey);

  /**
   * Returns {@code true} if the table contains a mapping with the specified
   * column.
   *
   * @param columnKey key of column to search for
   */
  boolean containsColumn(@Nullable Object columnKey);

  /**
   * Returns {@code true} if the table contains a mapping with the specified
   * value. May run in O(n) or O(1) time depending on the implementation.
   *
   * @param value value to search for
   */
  boolean containsValue(@Nullable Object value);

  /**
   * Returns the values corresponding to the given row and column keys, or
   * an empty collection if no such mapping exists.
   *
   * @param rowKey    key of row to search for
   * @param columnKey key of column to search for
   */
  Collection<V> get(@Nullable Object rowKey, @Nullable Object columnKey);

  /**
   * Returns {@code true} if the table contains no mappings.
   */
  boolean isEmpty();

  /**
   * Returns the number of row key / column key / value mappings in the table.
   */
  int size();

  /**
   * Compares the specified object with this table for equality. Two tables are
   * equal when their cell views, as returned by {@link #cellSet}, are equal.
   * In general, two Multitables with identical key-key-value mappings may or may not be equal,
   * depending on the implementation. For example, two SetMultitable instances with the same
   * key-value mappings are equal, but equality of two ListMultitable instances depends on the
   * ordering of the values for each key.

   * A non-empty SetMultitable cannot be equal to a non-empty ListMultitable,
   * since their asMap() views contain unequal collections as values. However, any two empty
   * multitables are equal, because they both have empty asMap() views.
   */
  @Override
  boolean equals(@Nullable Object obj);

  /**
   * Returns the hash code for this table. The hash code of a table is defined
   * as the hash code of its cell view, as returned by {@link #cellSet}.
   */
  @Override
  int hashCode();

  // Mutators

  /**
   * Removes all mappings from the table.
   */
  void clear();

  /**
   * Associates the specified value with the specified keys.
   *
   * @param rowKey    row key that the value should be associated with
   * @param columnKey column key that the value should be associated with
   * @param value     value to be associated with the specified keys
   * @return true if the method increased the size of the table, or false if the table already
   * contained the (row, column, value) tuple and doesn't allow duplicates
   */
  boolean put(R rowKey, C columnKey, V value);

  /**
   * Associates all specified values with the specified keys.
   *
   * @param rowKey    row key that the value should be associated with
   * @param columnKey column key that the value should be associated with
   * @param values    value to be associated with the specified keys
   * @return true if the table changed
   */
  boolean putAll(R rowKey, C columnKey, Iterable<? extends V> values);

  /**
   * Copies all mappings from the specified table to this table. The effect is
   * equivalent to calling {@link #put} with each row key / column key / value
   * mapping in {@code table}.
   *
   * @param table the table to add to this table
   * @return true if the table changed
   */
  boolean putAll(Multitable<? extends R, ? extends C, ? extends V> table);

  /**
   * Removes the mapping, if any, associated with the given keys and value.
   *
   * @param rowKey    row key of mapping to be removed
   * @param columnKey column key of mapping to be removed
   * @return true if the table changed
   */
  boolean remove(@Nullable Object rowKey, @Nullable Object columnKey, Object value);

  /**
   * Removes all mappings, if any, associated with the given keys.
   *
   * @param rowKey    row key of mapping to be removed
   * @param columnKey column key of mapping to be removed
   * @return true if the table changed
   */
  boolean removeAll(@Nullable Object rowKey, @Nullable Object columnKey);

  // Views

  /**
   * Returns a view of all mappings that have the given row key. For each row
   * key / column key / value mapping in the table with that row key, the
   * returned multimap associates the column key with the values. If no mappings in
   * the table have the provided row key, an empty multimap is returned.
   *
   * <p>Changes to the returned multimap will update the underlying table, and vice
   * versa.</p>
   *
   * @param rowKey key of row to search for in the table
   * @return the corresponding multimap from column keys to values
   */
  Multimap<C, V> row(R rowKey);

  /**
   * Returns a view of all mappings that have the given column key. For each row
   * key / column key / value mapping in the table with that column key, the
   * returned multimap associates the row key with the values. If no mappings in the
   * table have the provided column key, an empty multimap is returned.
   *
   * <p>Changes to the returned multimap will update the underlying table, and vice
   * versa.</p>
   *
   * @param columnKey key of column to search for in the table
   * @return the corresponding multimap from row keys to values
   */
  Multimap<R, V> column(C columnKey);

  /**
   * Returns a set of all row key / column key / value triplets. Changes to the
   * returned set will update the underlying table, and vice versa. The cell set
   * does not support the {@code add} or {@code addAll} methods.
   *
   * @return set of table cells consisting of row key / column key / value triplets
   */
  Set<Multicell<R, C, V>> cellSet();

  /**
   * Returns a set of row keys that have one or more values in the table.
   * Changes to the set will update the underlying table, and vice versa.
   *
   * @return set of row keys
   */
  Set<R> rowKeySet();

  /**
   * Returns a set of column keys that have one or more values in the table.
   * Changes to the set will update the underlying table, and vice versa.
   *
   * @return set of column keys
   */
  Set<C> columnKeySet();

  /**
   * Returns a collection of all values, which may contain duplicates. Changes
   * to the returned collection will update the underlying table, and vice
   * versa.
   *
   * @return collection of values
   */
  Collection<V> values();

  /**
   * Returns a view that associates each row key with the corresponding multimap from
   * column keys to values. Changes to the returned map will update this table.
   * The returned map does not support {@code put()} or {@code putAll()}, or
   * {@code setValue()} on its entries.
   *
   * <p>In contrast, the multimaps returned by {@code rowMap().get()} have the same
   * behavior as those returned by {@link #row}. Those multimaps may support {@code
   * setValue()}, {@code put()}, and {@code putAll()}.</p>
   *
   * @return a map view from each row key to a secondary multimap from column keys to values
   */
  Map<R, Multimap<C, V>> rowMap();

  /**
   * Returns a view that associates each column key with the corresponding multimap
   * from row keys to values. Changes to the returned map will update this
   * table. The returned map does not support {@code put()} or {@code putAll()},
   * or {@code setValue()} on its entries.
   *
   * <p>In contrast, the multimaps returned by {@code columnMap().get()} have the
   * same behavior as those returned by {@link #column}. Those multimaps may support
   * {@code setValue()}, {@code put()}, and {@code putAll()}.
   *
   * @return a map view from each column key to a secondary multimap from row keys to values
   */
  Map<C, Multimap<R, V>> columnMap();

  /**
   * Row key / column key / value triplet corresponding to a mapping in a table.
   */
  interface Multicell<R, C, V> {

    /**
     * Returns the row key of this cell.
     */
    @Nullable
    R getRowKey();

    /**
     * Returns the column key of this cell.
     */
    @Nullable
    C getColumnKey();

    /**
     * Returns the values of this cell.
     */
    @Nullable
    Collection<V> getValues();

    /**
     * Compares the specified object with this cell for equality. Two cells are
     * equal when they have equal row keys, column keys, and values.
     */
    @Override
    boolean equals(@Nullable Object obj);

    /**
     * Returns the hash code of this cell.
     *
     * <p>The hash code of a table cell is equal to {@link
     * Objects#hashCode}{@code (e.getRowKey(), e.getColumnKey(), e.getValues())}.
     */
    @Override
    int hashCode();
  }
}
