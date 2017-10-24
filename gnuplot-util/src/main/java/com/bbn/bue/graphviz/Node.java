package com.bbn.bue.graphviz;

import com.bbn.bue.common.StringUtils;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Very beta. Do not use for anything but throw-away code.
 */
@Beta
public final class Node {

  private final String name;
  private final String label;
  private final Shape shape;
  private final ImmutableSet<String> styles;

  Node(final String name, String label, Iterable<String> styles, Shape shape) {
    this.name = checkNotNull(name);
    checkArgument(!name.isEmpty());
    this.label = label;
    this.styles = ImmutableSet.copyOf(styles);
    this.shape = checkNotNull(shape);
  }

  public static Builder builderWithName(String name) {
    return new Builder(name);
  }

  public static Builder builderWithRandomName() {
    return new Builder("node_" + UUID.randomUUID().toString().replaceAll("-", "_"));
  }

  public String name() {
    return name;
  }

  public static final class Builder {

    private Builder(String name) {
      this.name = name;
    }

    public Builder withLabel(String label) {
      this.label = checkNotNull(label);
      return this;
    }

    public Builder withDottedBorder() {
      this.styles.add("dotted");
      return this;
    }

    public Builder withBoldBorder() {
      this.styles.add("bold");
      return this;
    }

    public Builder withShape(Shape shape) {
      this.shape = checkNotNull(shape);
      return this;
    }

    public Node build() {
      return new Node(name, label, styles, shape);
    }

    private final String name;
    private String label = null;
    private Shape shape = Shape.ELLIPSE;
    private List<String> styles = Lists.newArrayList();
  }

  public String toDot() {
    final List<String> attributes = Lists.newArrayList();
    if (label != null) {
      attributes.add("label=\"" + label + "\"");
    }
    if (!styles.isEmpty()) {
      if (styles.size() == 1) {
        attributes.add("style=" + Iterables.getFirst(styles, null));
      } else {
        throw new UnsupportedOperationException("Multiple styles not yet supported");
      }
    }

    attributes.add("shape=\"" + shape.toDot() + "\"");

    final String attributeString;
    if (!attributes.isEmpty()) {
      attributeString = "[" + StringUtils.CommaSpaceJoiner.join(attributes) + "]";
    } else {
      attributeString = "";
    }

    return name + " " + attributeString + ";";
  }

  // Guava Functions

  public static Function<Node, String> toDotFunction() {
    return new Function<Node, String>() {
      @Override
      public String apply(Node input) {
        return input.toDot();
      }
    };
  }

  public static Function<Node, String> nameFunction() {
    return new Function<Node, String>() {
      @Override
      public String apply(final Node input) {
        return input.name();
      }
    };
  }

  public enum Shape { BOX("box"), ELLIPSE("ellipse");
    Shape(String nameInDot) {
      this.nameInDot = checkNotNull(nameInDot);
    }

    public String toDot() {
      return nameInDot;
    }

    private final String nameInDot;
  }
}
