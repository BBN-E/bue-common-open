package com.bbn.bue.gnuplot;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.bbn.bue.gnuplot.GnuPlotUtils.gnuPlotString;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class StackedBarChart implements GnuPlottable {

  private final ImmutableList<StackedBar> stackedBars;
  private final ImmutableList<String> barSegmentNames;
  private final String title;
  private final Axis xAxis;
  private final Axis yAxis;
  private final double boxWidth;
  private final Key key;
  private final Grid grid;

  private StackedBarChart(
      Iterable<StackedBar> stackedBars, String title, Axis xAxis, Axis yAxis,
      List<String> barSegementNames, double boxWidth, Key key, Grid grid) {
    this.stackedBars = ImmutableList.copyOf(stackedBars);
    this.barSegmentNames = ImmutableList.copyOf(barSegementNames);
    assertBarsAndSegementsCompatible();
    this.title = checkNotNull(title);
    this.xAxis = checkNotNull(xAxis);
    this.yAxis = checkNotNull(yAxis);
    this.boxWidth = boxWidth;
    checkArgument(boxWidth > 0.0);
    this.key = checkNotNull(key);
    this.grid = checkNotNull(grid);
  }

  public static Builder builder() {
    return new Builder();
  }

  public File renderToEmptyDirectory(final File outputDirectory) throws IOException {
    throw new RuntimeException("update your code to use toPlotBundle");
  }

  private void assertBarsAndSegementsCompatible() {
    for (final StackedBar bar : stackedBars) {
      checkArgument(bar.values().size() == barSegmentNames.size(),
          "There are %s bar segments defined, but bar %s has only %s segements",
          barSegmentNames.size(), bar, bar.values().size());
    }
  }

  @Override
  public PlotBundle toPlotBundle() {
    final PlotBundle.Builder ret = PlotBundle.builder();
    plotCommands(ret);
    return ret.build();
  }

  public static final class StackedBar {

    private final String label;
    private final List<Double> values;

    private StackedBar(String label, List<Double> values) {
      // optional
      this.label = label;
      this.values = ImmutableList.copyOf(values);
    }

    public static StackedBar create(String label, List<Double> values) {
      return new StackedBar(label, values);
    }

    public Optional<String> label() {
      return Optional.fromNullable(label);
    }

    public List<Double> values() {
      return values;
    }
  }

  public static final class Builder {

    private final ImmutableList.Builder<StackedBar> stackedBars = ImmutableList.builder();
    private final ImmutableList.Builder<String> barSegmentNames = ImmutableList.builder();
    private String title = null;
    private Axis xAxis = Axis.xAxis().build();
    private Axis yAxis = Axis.yAxis().build();
    private double boxWidth = 0.5;
    private Key key = Key.visibleKey().build();
    private Grid grid = NormalGrid.builder().build();

    private Builder() {
    }

    public Builder setTitle(String title) {
      this.title = checkNotNull(title);
      return this;
    }

    public Builder setXAxis(Axis axis) {
      checkArgument(axis.axisType == AxisType.X);
      this.xAxis = checkNotNull(axis);
      return this;
    }

    public Builder setYAxis(Axis axis) {
      checkArgument(axis.axisType == AxisType.Y);
      this.yAxis = checkNotNull(axis);
      return this;
    }

    public Builder setBoxWidth(double boxWidth) {
      this.boxWidth = boxWidth;
      return this;
    }

    public Builder addStackedBar(final StackedBar stackedBar) {
      stackedBars.add(stackedBar);
      return this;
    }

    public Builder addBarSegment(final String segmentName) {
      barSegmentNames.add(segmentName);
      return this;
    }

    public Builder addBarSegments(Iterable<String> segmentNames) {
      barSegmentNames.addAll(segmentNames);
      return this;
    }

    public Builder setKey(Key key) {
      this.key = checkNotNull(key);
      return this;
    }

    public Builder setGrid(Grid grid) {
      this.grid = checkNotNull(grid);
      return this;
    }

    public StackedBarChart build() {
      return new StackedBarChart(stackedBars.build(), title, xAxis, yAxis,
          barSegmentNames.build(), boxWidth, key, grid);
    }
  }

  private String buildData() {
    final StringBuilder sb = new StringBuilder();
    for (final StackedBar stackedBar : stackedBars) {
      sb.append(stackedBar.label().or(" "));
      for (final double val : stackedBar.values()) {
        sb.append("\t");
        sb.append(val);
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  private void plotCommands(final PlotBundle.Builder sb) {
    key.appendPlotCommands(sb);
    sb.append("set datafile separator '\\t'\n");
    sb.append("set style data histograms\n");
    sb.append("set style histogram rowstacked\n");
    sb.append("set boxwidth ").append(boxWidth).append(" relative\n");
    sb.append("set style fill solid 1.0 border -1\n");
    xAxis.appendCommands(sb);
    yAxis.appendCommands(sb);
    sb.append("plot '");
    sb.appendData(buildData());
    sb.append("' ");
    boolean first = true;
    for (int segmentIdx = 0; segmentIdx < barSegmentNames.size(); ++segmentIdx) {
      if (!first) {
        sb.append(", ''");
      } else {
        first = false;
      }
      final String segmentName = barSegmentNames.get(segmentIdx);
      sb.append(" using ").append(segmentIdx + 2).append(":xticlabels(1)")
          .append(" t ").append(gnuPlotString(segmentName));
    }
  }
}
