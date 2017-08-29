package com.bbn.bue.common.files;

import com.google.common.base.Optional;

import java.io.IOException;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Provides a mapping of keys to values designed to support key-value stores. The mapping is not
 * guaranteed to be immutable; see {@link ImmutableKeyValueSource} for an immutable derived
 * interface. In particular, the available keys can be changed in between a call to keySet() and a
 * call to get().
 *
 * This was originally implemented to support storing the linguistic analyses of many documents
 * together while abstracting away from how exactly they are stored (as individual files, in a
 * zip file, from a database).
 *
 * Some standard implementations are provided in {@link KeyValueSources}.
 *
 * @param <K> type of the key
 * @param <V> type of the value
 * @author Constantine Lignos, Ryan Gabbard
 */
public interface KeyValueSource<K, V> extends AutoCloseable {

  /**
   * Returns the set of keys currently present in the source. This may be expensive to compute, as
   * the backing source may not provide a set of the keys and such a set may be very large. If
   * only iteration is required, prefer {@link #keys()}.
   *
   * @see #keys()
   */
  @Nonnull
  Set<K> keySet() throws IOException;

  /**
   * Returns an iterable over the keys. If a set of the keys is required, prefer {@link #keySet()}.
   *
   * @see #keySet()
   */
  @Nonnull
  Iterable<K> keys() throws IOException;

  /**
   * Returns the value corresponding to a key or {@link Optional#absent()} if the key is not
   * present.
   */
  @Nonnull
  Optional<V> get(K key) throws IOException;

  /**
   * Returns the value corresponding to a key, throwing {@link NoSuchKeyException} if the key is not
   * present.
   */
  @Nonnull
  V getRequired(K key) throws IOException;

  // This override is necessary to change the exception signature from Exception
  @Override
  void close() throws IOException;
}
