package com.bbn.bue.common;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Stores one object (type type {@code T}) in the context if another object (of type {@code Ctx}).
 * This can be useful for hierarchical objects which themselve only store "downward" references.
 *
 * Two {@code InContext}s are equal if and only if both the item and its context are {@code
 * .equal()}.
 */
@Beta
public final class InContextOf<T, CtxT> {

  private final T item;
  private final CtxT context;

  private InContextOf(final T item, final CtxT context) {
    this.item = checkNotNull(item);
    this.context = checkNotNull(context);
  }

  public static <T, Ctx> InContextOf<T, Ctx> createXInContextOfY(T item, Ctx context) {
    return new InContextOf<T, Ctx>(item, context);
  }

  public CtxT context() {
    return context;
  }

  public T item() {
    return item;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(item, context);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final InContextOf other = (InContextOf) obj;
    return Objects.equal(this.item, other.item)
        && Objects.equal(this.context, other.context);
  }

  public static <F, T, C> Function<InContextOf<F, C>, InContextOf<T, C>> passThroughContext(
      final Function<F, T> f) {
    return new Function<InContextOf<F, C>, InContextOf<T, C>>() {
      @Override
      public InContextOf<T, C> apply(final InContextOf<F, C> input) {
        return InContextOf.createXInContextOfY(f.apply(input.item()), input.context());
      }
    };
  }
}
