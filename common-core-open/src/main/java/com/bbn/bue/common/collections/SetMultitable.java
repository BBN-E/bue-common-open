package com.bbn.bue.common.collections;

import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * A {@link Multitable} which cannot hold duplicate row key, column key, value triples.
 */
public interface SetMultitable<R, C, V> extends Multitable<R, C, V> {

  @Override
  Collection<V> get(@Nullable Object rowKey, @Nullable Object columnKey);

  Set<V> getAsSet(@Nullable Object rowKey, @Nullable Object columnKey);

  @Override
  Multimap<C, V> row(R rowKey);

  @Override
  Multimap<R, V> column(C columnKey);

  @Override
  Collection<V> values();

  @Override
  Set<Multicell<R, C, V>> cellSet();

  interface SetMulticell<R, C, V> extends Multicell<R, C, V> {
    @Override
    Set<V> getValues();
  }
}
