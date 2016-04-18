package com.bbn.bue.common.evaluation;

import com.bbn.bue.common.Inspector;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An inspector tree node which applies some transformation to its input before passing it on to its
 * consumers.  See {@link InspectorTreeDSL}.
 */
@Beta
class TransformNode<InT, OutT> extends InspectorTreeNode<OutT> implements
    Inspector<InT> {

  private final Function<? super InT, ? extends OutT> transform;

  TransformNode(final Function<? super InT, ? extends OutT> transform) {
    this.transform = checkNotNull(transform);
  }

  @Override
  public void inspect(final InT item) {
    final OutT transformed;
    try {
      transformed = transform.apply(item);
    } catch (Exception e) {
      throw new RuntimeException("Exception while applying transformation " + transform, e);
    }
    for (final Inspector<OutT> subInspector : consumers()) {
      subInspector.inspect(transformed);
    }
  }
}
