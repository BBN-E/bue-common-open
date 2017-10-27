package com.bbn.bue.gnuplot;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.io.IOException;

import static com.bbn.bue.gnuplot.GnuPlotUtils.gnuPlotString;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Beta
public final class BarChart implements GnuPlottable {

  private final ImmutableList<Bar> bars;
  private final String title;
  private final Axis xAxis;
  private final Axis yAxis;
  private final double boxWidth;
  private final boolean showKey;
  private final Grid grid;

  private BarChart(Iterable<Bar> bars, Axis xAxis, Axis yAxis, String title,
      double boxWidth, boolean showKey, Grid grid) {
    this.bars = ImmutableList.copyOf(bars);
    this.xAxis = checkNotNull(xAxis);
    this.yAxis = checkNotNull(yAxis);
    // optional
    this.title = title;
    this.boxWidth = boxWidth;
    checkArgument(boxWidth > 0.0);
    this.showKey = showKey;
    this.grid = checkNotNull(grid);
  }

  @Deprecated
  public File renderToEmptyDirectory(final File outputDirectory)
      throws IOException {
    throw new RuntimeException("Update your program to use toPlotBundle");
  }

  public static Builder builder() {
    return new Builder();
  }

  public ImmutableList<Bar> bars() {
    return bars;
  }

  @Override
  public PlotBundle toPlotBundle() {
    final PlotBundle.Builder ret = PlotBundle.builder();
    plotCommands(ret);
    return ret.build();
  }


  public static final class Bar {

    private final String label;
    private final double value;

    private Bar(String label, double value) {
      // optional, may be null
      this.label = label;
      this.value = value;
    }

    public Optional<String> label() {
      return Optional.fromNullable(label);
    }

    public double value() {
      return value;
    }

    public static Builder builder(double value) {
      return new Builder(value);
    }

    public static class Builder {

      // optional
      private String label = null;
      private final double value;

      private Builder(double value) {
        this.value = value;
      }

      public Builder setLabel(String label) {
        this.label = checkNotNull(label);
        return this;
      }

      public Bar build() {
        return new Bar(label, value);
      }
    }
  }

  public static class Builder {

    private ImmutableList.Builder<Bar> bars = ImmutableList.builder();
    private String title = null;
    private Axis xAxis = Axis.xAxis().build();
    private Axis yAxis = Axis.yAxis().build();
    private double boxWidth = 0.5;
    private boolean showKey = true;
    private Grid grid = NormalGrid.builder().build();

    private Builder() {
    }

    public Builder addBar(Bar bar) {
      this.bars.add(bar);
      return this;
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

    public Builder hideKey() {
      this.showKey = false;
      return this;
    }

    public Builder setGrid(Grid grid) {
      this.grid = checkNotNull(grid);
      return this;
    }

    public BarChart build() {
      return new BarChart(bars.build(), xAxis, yAxis, title, boxWidth, showKey, grid);
    }
  }

  private String data() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < bars.size(); ++i) {
      final Bar bar = bars.get(i);
      sb.append(i);
      sb.append("\t");
      sb.append(gnuPlotString(bar.label().or("")));
      sb.append("\t");
      sb.append(bar.value());
      sb.append("\n");
    }
    return sb.toString();
  }

  private void plotCommands(PlotBundle.Builder pb) {
    if (title != null) {
      pb.append("set title ").append(gnuPlotString(title)).append("\n");
    }
    xAxis.appendCommands(pb);
    yAxis.appendCommands(pb);
    grid.appendPlotCommands(pb);
    pb.append("set style fill solid\n");
    pb.append("set boxwidth ").append(boxWidth).append("\n");
    if (showKey) {
      pb.append("set key\n");
    } else {
      pb.append("unset key\n");
    }
    if (!bars.isEmpty()) {
      pb.append("plot '").appendData(data()).append("' using 1:3:xtic(2) with boxes\n");
    } else {
      if (yAxis.range().isPresent()) {
        pb.append("plot ").append(yAxis.range().get().upperEndpoint() + 1).append("with lines\n");
      } else {
        pb.append("set yrange[0:1]\nplot 2 with lines\n");
      }
    }
  }
}
