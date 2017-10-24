package com.bbn.bue.graphviz;

import com.bbn.bue.common.StringUtils;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

import static com.google.common.collect.Iterables.transform;

/**
 * Created by rgabbard on 4/16/15.
 */
final class SameRankGroup {

  final ImmutableSet<Node> nodesInGroup;

  private SameRankGroup(final Iterable<Node> nodesInGroup) {
    this.nodesInGroup = ImmutableSet.copyOf(nodesInGroup);
  }

  public static SameRankGroup rankTogether(Iterable<Node> nodes) {
    return new SameRankGroup(nodes);
  }

  public ImmutableSet<Node> nodes() {
    return nodesInGroup;
  }

  public static Function<SameRankGroup, String> toDotFunction() {
    return new Function<SameRankGroup, String>() {
      @Override
      public String apply(final SameRankGroup input) {
        return "{rank=same; " + StringUtils.spaceJoiner().join(
            transform(input.nodes(), Node.nameFunction())) + "}";
      }
    };
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(nodesInGroup);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final SameRankGroup other = (SameRankGroup) obj;
    return Objects.equal(this.nodesInGroup, other.nodesInGroup);
  }
}
