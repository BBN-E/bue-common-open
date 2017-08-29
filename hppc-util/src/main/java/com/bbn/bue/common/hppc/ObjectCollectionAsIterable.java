package com.bbn.bue.common.hppc;

import com.carrotsearch.hppc.ObjectCollection;
import com.carrotsearch.hppc.cursors.ObjectCursor;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

final class ObjectCollectionAsIterable<T> implements Iterable<T> {

  private final ObjectCollection<T> hppcObjectCollection;

  ObjectCollectionAsIterable(final ObjectCollection<T> hppcObjectCollection) {
    this.hppcObjectCollection = checkNotNull(hppcObjectCollection);
  }

  @Override
  public Iterator<T> iterator() {
    return new ObjectCollectionIterator<>(hppcObjectCollection.iterator());
  }

  private static final class ObjectCollectionIterator<T> implements Iterator<T> {

    private final Iterator<ObjectCursor<T>> cursor;

    public ObjectCollectionIterator(final Iterator<ObjectCursor<T>> cursor) {
      this.cursor = checkNotNull(cursor);
    }

    @Override
    public boolean hasNext() {
      return cursor.hasNext();
    }

    @Override
    public T next() {
      return cursor.next().value;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
