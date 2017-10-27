package com.bbn.bue.common.files;

/**
 * Convenience class for implementing the {@link ImmutableKeyValueSource} interface.  Currently it
 * doesn't do anything useful, but it might someday.
 *
 * See {@link KeyValueSource} for general documentation of the key-value classes.
 *
 * @author Constantine Lignos, Ryan Gabbard
 */
public abstract class AbstractImmutableKeyValueSource<K, V> extends AbstractKeyValueSource<K, V>
    implements ImmutableKeyValueSource<K, V> {

}
