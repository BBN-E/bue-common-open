package com.bbn.bue.common;

import com.google.common.annotations.Beta;

/**
 * An object which can inspect some item (of type {@code T}).
 * {@link #finish()}  is expected to be called when there are no more items to inspect. What happens
 * if more items are inspected after finishing is not defined by the interface contract.
 *
 * @param <T>    Contravariant
 */
@Beta
public interface Inspector<T> extends Finishable {

  void inspect(T item);
}
