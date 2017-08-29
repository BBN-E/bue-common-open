package com.bbn.bue.common.files;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * Provides an implementation of {@link KeyValueSource#getRequired(Object)} for convenience.
 *
 * See {@link KeyValueSource} for general documentation of the key-value classes.
 *
 * @author Constantine Lignos, Ryan Gabbard
 */
public abstract class AbstractKeyValueSource<K, V> implements KeyValueSource<K, V> {

  @Override
  @Nonnull
  public V getRequired(final K key) throws IOException {
    final Optional<V> value = get(key);
    if (value.isPresent()) {
      return value.get();
    } else {
      throw new NoSuchKeyException("No such key: " + key.toString());
    }
  }

  @Override
  @Nonnull
  public ImmutableSet<K> keySet() throws IOException {
    return FluentIterable.from(keys()).toSet();
  }
}
