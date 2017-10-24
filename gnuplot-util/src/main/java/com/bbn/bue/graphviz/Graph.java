package com.bbn.bue.graphviz;

import com.bbn.bue.common.StringUtils;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.transform;

/**
 * Very beta. Do not use for anything but throw-away code.
 */
@Beta
public final class Graph {

  private final String name;
  private final ImmutableSet<Node> nodes;
  private final ImmutableSet<Edge> edges;
  private final boolean directed;
  private final RankDirection rankDirection;
  private final double rankSep;
  private final ImmutableSet<SameRankGroup> sameRankGroups;

  enum RankDirection {
    TOP_BOTTOM {
      @Override
      public String dotString() {
        return "TB";
      }
    }, LEFT_RIGHT {
      @Override
      public String dotString() {
        return "LR";
      }
    };

    public abstract String dotString();
  }

  Graph(String name, boolean directed,
      Iterable<Node> nodes, Iterable<Edge> edges,
      RankDirection rankDirection, double rankSep,
      Iterable<SameRankGroup> sameRankGroups) {
    this.name = checkNotNull(name);
    checkArgument(!name.isEmpty());
    this.directed = directed;
    this.nodes = ImmutableSet.copyOf(nodes);
    this.edges = ImmutableSet.copyOf(edges);
    this.rankDirection = checkNotNull(rankDirection);
    this.rankSep = rankSep;
    checkArgument(rankSep > 0.0, "Rank separation must be positive");
    for (final Edge e : edges) {
      checkArgument(this.nodes.contains(e.source()));
      checkArgument(this.nodes.contains(e.target()));
    }
    this.sameRankGroups = ImmutableSet.copyOf(sameRankGroups);
  }

  public static Builder createDirected(String name) {
    return new Builder(name, true);
  }

  public static final class Builder {

    private final ImmutableSet.Builder<Node> nodes = ImmutableSet.builder();
    private final ImmutableSet.Builder<Edge> edges = ImmutableSet.builder();
    // this specifies groups of nodes which are constrained to have the same 'rank' (layer)
    // when rendered by dot
    private final ImmutableSet.Builder<SameRankGroup> sameRankGroups = ImmutableSet.builder();
    private final boolean directed;
    private final String name;
    private double rankSep = 0.75;
    private RankDirection rankDirection = RankDirection.TOP_BOTTOM;

    public Builder(String name, boolean directed) {
      this.name = checkNotNull(name);
      checkArgument(!name.isEmpty(), "Graph name cannot be empty");
      this.directed = directed;
    }

    public Builder addNode(Node node) {
      nodes.add(checkNotNull(node));
      return this;
    }

    public Builder addEdge(Edge edge) {
      edges.add(checkNotNull(edge));
      return this;
    }

    public Builder rankTopToBottom() {
      rankDirection = RankDirection.TOP_BOTTOM;
      return this;
    }

    public Builder rankLeftToRight() {
      rankDirection = RankDirection.LEFT_RIGHT;
      return this;
    }

    public Builder rankSeparationInInches(double rankSep) {
      checkArgument(rankSep > 0.0);
      this.rankSep = rankSep;
      return this;
    }

    public Builder rankTogether(Iterable<Node> nodes) {
      sameRankGroups.add(SameRankGroup.rankTogether(nodes));
      return this;
    }

    public Graph build() {
      return new Graph(name, directed, nodes.build(), edges.build(),
          rankDirection, rankSep, sameRankGroups.build());
    }

    public void addNodes(final Iterable<Node> nodes) {
      for (final Node node : nodes) {
        addNode(node);
      }
    }
  }

  public String toDot() {
    final String graphElement = directed ? "digraph" : "graph";
    return graphElement + " \"" + name + "\" {\n"
        + "rankdir=" + rankDirection.dotString() + ";\n"
        + "ranksep=" + Double.toString(rankSep) + ";\n"
        + StringUtils.unixNewlineJoiner().join(
        transform(nodes, Node.toDotFunction()))
        + StringUtils.unixNewlineJoiner().join(transform(edges, Edge
        .toDotFunction()))
        + StringUtils.unixNewlineJoiner()
        .join(transform(sameRankGroups, SameRankGroup.toDotFunction()))
        + "}";
  }

}
