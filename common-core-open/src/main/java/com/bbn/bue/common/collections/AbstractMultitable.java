package com.bbn.bue.common.collections;

import javax.annotation.Nullable;

/**
 * Abstract class for implementing {@link Multitable}s which satisfy the {@link #equals(Object)}
 * and {@link #hashCode()} contract.
 */
public abstract class AbstractMultitable<R,C,V> implements Multitable<R,C,V> {
  @Override
  public final int hashCode() {
    return cellSet().hashCode();
  }

  @Override
  public final boolean equals(@Nullable Object obj){
    if (obj == this) {
      return true;
    } else if (obj instanceof Multitable) {
      final Multitable<?, ?, ?> that = (Multitable<?, ?, ?>) obj;
      return this.cellSet().equals(that.cellSet());
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return rowMap().toString();
  }
}
