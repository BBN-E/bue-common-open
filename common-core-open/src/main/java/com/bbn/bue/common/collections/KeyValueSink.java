package com.bbn.bue.common.collections;

import java.util.Map;

/**
 * Something which consumes mappings between objects. This is mostly for eliding the difference
 * between {@link java.util.Map}s and {@link com.google.common.collect.Multimap}s in some cases.
 */
public interface KeyValueSink<K, V> {

  KeyValueSink<K, V> put(K key, V value);

  KeyValueSink<K, V> put(Map.Entry<K, V> entry);

  KeyValueSink<K, V> putAll(Iterable<? extends Map.Entry<? extends K, ? extends V>> entries);
}
