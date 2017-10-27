package com.bbn.bue.gnuplot;

import com.google.common.base.Optional;
import com.google.common.collect.Range;

import static com.bbn.bue.gnuplot.GnuPlotUtils.gnuPlotString;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class Axis {

  // package private
  final AxisType axisType;
  private final Range<Double> range;
  private final String label;
  private final boolean rotateLabels;

  private Axis(AxisType axisType, Range<Double> range, String label, boolean rotateLabels) {
    this.axisType = checkNotNull(axisType);
    // range may be unspecified
    this.range = range;
    // label may be unspecified
    this.label = label;
    this.rotateLabels = rotateLabels;
  }

  public void appendCommands(PlotBundle.Builder pb) {
    final String xy = axisType.letter();
    if (label != null) {
      pb.append("set ").append(xy).append("label ")
          .append(gnuPlotString(label)).append("\n");
    }
    if (range != null) {
      pb.append("set ").append(xy).append("range [")
          .append(range.lowerEndpoint()).append(":")
          .append(range.upperEndpoint()).append("]\n");
    }
    if (rotateLabels) {
      pb.append("set ").append(xy).append("tics rotate\n");
    }
  }

  public static Builder xAxis() {
    return new Builder(AxisType.X);
  }

  public static Builder yAxis() {
    return new Builder(AxisType.Y);
  }

  public Optional<Range<Double>> range() {
    return Optional.fromNullable(range);
  }

  public static class Builder {

    private final AxisType axisType;
    private Range<Double> range = null;
    private String label = null;
    private boolean rotateLabels = false;

    private Builder(AxisType axisType) {
      this.axisType = checkNotNull(axisType);
    }

    public Builder setRange(Range<Double> range) {
      this.range = checkNotNull(range);
      checkArgument(range.hasLowerBound() && range.hasUpperBound(), "Axis ranges must be finite");
      return this;
    }

    public Builder setLabel(String label) {
      this.label = checkNotNull(label);
      return this;
    }

    public Builder rotateLabels() {
      this.rotateLabels = true;
      return this;
    }

    public Axis build() {
      return new Axis(axisType, range, label, rotateLabels);
    }
  }
}
