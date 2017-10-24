package com.bbn.bue.gnuplot;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

@Beta
public final class ScatterData {

  private ScatterData(final String title, final List<Point2D> points, final Color color) {
    this.title = Optional.fromNullable(title);
    checkArgument(!points.isEmpty(), "Empty lines not supported");
    this.points = ImmutableList.copyOf(points);
    this.color = Optional.fromNullable(color);
  }

  public static ScatterData create(final String title, final List<Point2D> points) {
    return new ScatterData(title, points, null);
  }

  public static ScatterData createUnlabelled(final List<Point2D> points) {
    return new ScatterData(null, points, null);
  }

  public Optional<String> title() {
    return title;
  }

  public List<Point2D> points() {
    return points;
  }

  public Optional<Color> color() {
    return color;
  }

  private final Optional<String> title;
  private final List<Point2D> points;
  private final Optional<Color> color;

  public static final Function<ScatterData, Integer> numPointsFunction() {
    return new Function<ScatterData, Integer>() {
      @Override
      public Integer apply(final ScatterData data) {
        return data.points().size();
      }
    };
  }

  public static Builder fromPoints(List<Point2D> points) {
    return new Builder(points);
  }

  public static final class Builder {
    private String title;
    private final List<Point2D> points;
    private Color color;

    private Builder(final List<Point2D> points) {
      this.points = Lists.newArrayList(points);
      title = null;
      color = null;
    }

    public Builder withTitle(final String title) {
      this.title = title;
      return this;
    }

    public Builder withColor(final Color color) {
      this.color = color;
      return this;
    }

    public ScatterData build() {
      return new ScatterData(title, points, color);
    }
  }
}
