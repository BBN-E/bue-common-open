package com.bbn.bue.common.collections;

import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * A {@link Multitable} which can hold duplicate row key, column key, value triples.
 */
public interface ListMultitable<R, C, V> extends Multitable<R, C, V> {

  @Override
  Collection<V> get(@Nullable Object rowKey, @Nullable Object columnKey);

  List<V> getAsList(@Nullable Object rowKey, @Nullable Object columnKey);

  @Override
  Multimap<C, V> row(R rowKey);

  @Override
  Multimap<R, V> column(C columnKey);

  @Override
  Collection<V> values();

  @Override
  Set<Multicell<R, C, V>> cellSet();

  interface ListMulticell<R, C, V> extends Multicell<R, C, V> {
    @Override
    List<V> getValues();
  }
}
