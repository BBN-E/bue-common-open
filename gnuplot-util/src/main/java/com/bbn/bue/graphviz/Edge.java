package com.bbn.bue.graphviz;

import com.bbn.bue.common.StringUtils;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Very beta. Do not use for anything but throw-away code.
 */
@Beta
public final class Edge {

  private final Node source;
  private final Node target;
  private final String label;
  private final ImmutableSet<String> styles;

  Edge(Node source, Node target, String label, Iterable<String> styles) {
    this.source = checkNotNull(source);
    this.target = checkNotNull(target);
    this.label = label;
    this.styles = ImmutableSet.copyOf(styles);
  }

  public static Builder fromTo(Node source, Node target) {
    return new Builder(source, target);
  }

  public Node source() {
    return source;
  }

  public Node target() {
    return target;
  }

  public String toDot() {
    final List<String> attributes = Lists.newArrayList();
    if (!styles.isEmpty()) {
      if (styles.size() == 1) {
        attributes.add("style=" + Iterables.getFirst(styles, null));
      } else {
        throw new UnsupportedOperationException("Multiple styles not yet supported");
      }
    }
    if (label != null) {
      attributes.add("label=\"" + label + "\"");
    }

    final String attributesString;

    if (!attributes.isEmpty()) {
      attributesString = "[" + StringUtils.CommaSpaceJoiner.join(attributes) + "]";
    } else {
      attributesString = "";
    }

    return source.name() + " -> " + target.name() + attributesString + ";";
  }

  public static Function<Edge, String> toDotFunction() {
    return new Function<Edge, String>() {
      @Override
      public String apply(Edge input) {
        return input.toDot();
      }
    };
  }

  public static final class Builder {

    private final Node source;
    private final Node target;
    private final List<String> styles = Lists.newArrayList();
    private String label = null;

    private Builder(Node source, Node target) {
      this.source = checkNotNull(source);
      this.target = checkNotNull(target);
    }

    public Builder withLabel(String label) {
      this.label = checkNotNull(label);
      return this;
    }

    public Builder dotted() {
      this.styles.add("dotted");
      return this;
    }

    public Edge build() {
      return new Edge(source, target, label, styles);
    }
  }
}
