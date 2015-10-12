package com.bbn.bue.common;

import com.google.common.annotations.Beta;

/**
 * An object which can observe some pairs of items (of types {@code X} and {@code Y}) in some
 * context (of type {@code Ctx}). {@link #finish()}  is expected to be called when there are no more
 * items to observe. What happens if more items are observed after finishing is not defined by the
 * interface contract.
 */
@Beta
public interface PairedContextObserver<CtxT, X, Y> extends Finishable {

  void observe(CtxT id, X left, Y right);
}
