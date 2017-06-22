package com.bbn.bue.common.evaluation;

import com.bbn.bue.common.Finishable;
import com.bbn.bue.common.Inspector;

import com.google.common.annotations.Beta;

/**
 * An inspection tree node which inspects things. See {@link InspectorTreeDSL}.
 */
@Beta
public class InspectionNode<InT> extends InspectorTreeNode<InT> implements Inspector<InT>,
    Finishable {

  @Override
  public void inspect(final InT item) {
    for (final Inspector<InT> consumer : consumers()) {
      consumer.inspect(item);
    }
  }
}
