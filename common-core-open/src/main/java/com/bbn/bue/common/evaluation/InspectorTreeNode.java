package com.bbn.bue.common.evaluation;

import com.bbn.bue.common.Finishable;
import com.bbn.bue.common.Inspector;

import com.google.common.annotations.Beta;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * A node in an inspector tree. See {@link InspectorTreeDSL}. While this can't be package-private,
 * users should never sub-class this class.
 *
 * This lacks the usual {@code Abstract} prefix for brevity in use and because it's not meant to be
 * derived from.
 *
 * @param <OutT> The output type of this node.
 */
@Beta
public abstract class InspectorTreeNode<OutT> implements Finishable {

  private final List<Inspector<OutT>> consumers = Lists.newArrayList();

  protected List<Inspector<OutT>> consumers() {
    return Collections.unmodifiableList(consumers);
  }

  // Inspector is contravariant in its type
  @SuppressWarnings("unchecked")
  void registerConsumer(Inspector<? super OutT> subInspector) {
    consumers.add((Inspector<OutT>) subInspector);
  }

  @Override
  public final void finish() throws IOException {
    for (final Inspector<OutT> consumer : consumers) {
      consumer.finish();
    }
  }
}
