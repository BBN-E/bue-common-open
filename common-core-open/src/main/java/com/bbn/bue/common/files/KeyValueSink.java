package com.bbn.bue.common.files;

import java.io.IOException;

/**
 * Something which accepts key value pairs. Typically this will write to some data store
 * which can later be opened as a {@link KeyValueSource}, but this is not required.
 *
 * Some standard implementations are provided in {@link KeyValueSinks}.
 *
 * @param <K> type of the key
 * @param <V> type of the value
 * @author Constantine Lignos, Ryan Gabbard
 */
public interface KeyValueSink<K, V> extends AutoCloseable {

  /**
   * Put the specified key and value.
   */
  void put(K key, V value) throws IOException;

  // This override is necessary to change the exception signature from Exception
  @Override
  void close() throws IOException;
}
