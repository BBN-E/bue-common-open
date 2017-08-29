package com.bbn.bue.common.files;

/**
 * An immutable {@link KeyValueSource}. It is guaranteed that for a given instance, neither the keys
 * nor their corresponding values will change if the backing source is not modified. However,
 * all behavior is undefined if the backing source is modified.
 *
 * @author Constantine Lignos, Ryan Gabbard
 */
public interface ImmutableKeyValueSource<K, V> extends KeyValueSource<K, V> {

}
