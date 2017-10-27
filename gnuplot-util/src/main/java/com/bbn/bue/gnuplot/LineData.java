package com.bbn.bue.gnuplot;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Beta
public final class LineData {

  private LineData(final String title, final List<Point2D> points) {
    this.title = checkNotNull(title);
    this.points = ImmutableList.copyOf(points);
    checkArgument(!points.isEmpty(), "Empty lines not supported");
  }

  public static LineData from(final String key, final List<Point2D> points) {
    return new LineData(key, points);
  }

  public String title() {
    return title;
  }

  public List<Point2D> points() {
    return points;
  }

  private final String title;
  private final List<Point2D> points;

  public static final Function<LineData, Integer> NumPoints = new Function<LineData, Integer>() {
    @Override
    public Integer apply(final LineData data) {
      return data.points().size();
    }
  };
}
