package com.bbn.bue.common;

import com.google.common.annotations.Beta;

/**
 * An object which can observe some item (of type {@code T}) in some context (of type {@code Ctx}).
 * {@link #finish()}  is expected to be called when there are no more items to observe. What happens
 * if more items are observed after finishing is not defined by the interface contract.
 *
 * @param <CtxT> Covariant
 * @param <T>    Covariant
 */
@Beta
public interface UnaryContextObserver<CtxT, T> extends Finishable {

  void observe(CtxT id, T item);
}
