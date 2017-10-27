package com.bbn.bue.gnuplot;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static com.bbn.bue.gnuplot.GnuPlotUtils.gnuPlotString;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Beta
public final class ClusteredBarChart implements GnuPlottable {

  private final ImmutableList<ClusteredBar> clusteredBars;
  private final ImmutableList<String> barClusterNames;
  private final Optional<Palette> palette;
  private final String title;
  private final Axis xAxis;
  private final Axis yAxis;
  private final double boxWidth;
  private final double clusterGap;
  private final Key key;
  private final Grid grid;

  private ClusteredBarChart(final Iterable<ClusteredBar> clusteredBars,
      final Iterable<String> barClusterNames, final Palette palette,
      final String title, final Axis xAxis, final Axis yAxis,
      final double boxWidth, final double clusterGap, final Key key, final Grid grid) {
    this.clusteredBars = ImmutableList.copyOf(clusteredBars);
    this.barClusterNames = ImmutableList.copyOf(barClusterNames);
    this.palette = Optional.fromNullable(palette);
    assertBarsAndClustersCompatible();
    this.title = title;
    this.xAxis = checkNotNull(xAxis);
    this.yAxis = checkNotNull(yAxis);
    this.boxWidth = boxWidth;
    checkArgument(boxWidth > 0.0);
    this.clusterGap = clusterGap;
    checkArgument(clusterGap > 0.0);
    this.key = checkNotNull(key);
    this.grid = checkNotNull(grid);
  }

  public static Builder builder() {
    return new Builder();
  }

  @Deprecated
  public File renderToEmptyDirectory(final File outputDirectory)
      throws IOException {
    throw new RuntimeException("Update your program to use toPlotBundle");
  }

  private void assertBarsAndClustersCompatible() {
    for (final ClusteredBar bar : clusteredBars) {
      checkArgument(bar.values().size() == barClusterNames.size(),
          "There are %s bar clusters defined, but bar %s has only %s clusters",
          barClusterNames.size(), bar, bar.values().size());
    }
  }



  @Override
  public PlotBundle toPlotBundle() {
    final PlotBundle.Builder ret = PlotBundle.builder();
    plotCommands(ret);
    return ret.build();
  }

  public static final class ClusteredBar {
    private final String label;
    private final List<Double> values;

    private ClusteredBar(final String label, final List<Double> values) {
      this.label = label;
      this.values = ImmutableList.copyOf(values);
    }

    public static ClusteredBar create(final String label, final List<Double> values) {
      checkNotNull(label);
      return new ClusteredBar(label, values);
    }

    public static ClusteredBar createUnlabelled(final List<Double> values) {
      return new ClusteredBar(null, values);
    }

    public Optional<String> label() {
      return Optional.fromNullable(label);
    }

    public List<Double> values() {
      return values;
    }
  }

  public static final class Builder {
    private final ImmutableList.Builder<ClusteredBar> clusteredBars = ImmutableList.builder();
    private final ImmutableList.Builder<String> barClusterNames = ImmutableList.builder();
    private Palette palette = null;
    private String title = null;
    private Axis xAxis = Axis.xAxis().build();
    private Axis yAxis = Axis.yAxis().build();
    private double boxWidth = 0.5;
    private double clusterGap = 1.0;
    private Key key = Key.visibleKey().build();
    private Grid grid = NormalGrid.builder().build();

    private Builder() {
    }

    public Builder setTitle(final String title) {
      this.title = checkNotNull(title);
      return this;
    }

    public Builder setXAxis(final Axis axis) {
      checkArgument(axis.axisType == AxisType.X);
      this.xAxis = checkNotNull(axis);
      return this;
    }

    public Builder setYAxis(final Axis axis) {
      checkArgument(axis.axisType == AxisType.Y);
      this.yAxis = checkNotNull(axis);
      return this;
    }

    public Builder setBoxWidth(final double boxWidth) {
      this.boxWidth = boxWidth;
      return this;
    }

    public Builder setClusterGap(final double clusterGap) {
      this.clusterGap = clusterGap;
      return this;
    }

    public Builder addClusteredBar(final ClusteredBar clusteredBar) {
      clusteredBars.add(clusteredBar);
      return this;
    }

    public Builder addBarCluster(final String clusterName) {
      barClusterNames.add(clusterName);
      return this;
    }

    public Builder addBarClusters(Iterable<String> clusterNames) {
      barClusterNames.addAll(clusterNames);
      return this;
    }

    public Builder withPalette(final Palette palette) {
      this.palette = palette;
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

    public ClusteredBarChart build() {
      return new ClusteredBarChart(clusteredBars.build(), barClusterNames.build(), palette, title, xAxis, yAxis,
          boxWidth, clusterGap, key, grid);
    }
  }

  private String buildData() {
    final StringBuilder sb = new StringBuilder();
    for (final ClusteredBar clusteredBar : clusteredBars) {
      sb.append(clusteredBar.label().or(" "));
      for (final double val : clusteredBar.values()) {
        sb.append("\t");
        sb.append(val);
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  private void plotCommands(final PlotBundle.Builder sb) {
    key.appendPlotCommands(sb);
    if (title != null) {
      sb.append("set title ").append(gnuPlotString(title)).append("\n");
    }

    sb.append("set datafile separator '\\t'\n");
    sb.append("set style data histograms\n");
    sb.append("set style histogram cluster gap ").append(clusterGap).append("\n");
    sb.append("set boxwidth ").append(boxWidth).append("\n");
    sb.append("set style fill solid 1.0 border -1\n");
    xAxis.appendCommands(sb);
    yAxis.appendCommands(sb);
    sb.append("plot '");
    sb.appendData(buildData());
    sb.append("' ");

    final Optional<Iterator<Color>> colorList =
        palette.isPresent()? Optional.of(palette.get().infinitePaletteLoop().iterator()) : Optional.<Iterator<Color>>absent();

    boolean first = true;
    for (int clusterIdx = 0; clusterIdx < barClusterNames.size(); ++clusterIdx) {
      if (!first) {
        sb.append(", ''");
      } else {
        first = false;
      }
      final String clusterName = barClusterNames.get(clusterIdx);
      sb.append(" using ").append(clusterIdx + 2).append(":xtic(1)")
          .append(" t ").append(gnuPlotString(clusterName));

      if(colorList.isPresent()) {
        sb.append(" lc rgb " + colorList.get().next().asQuotedColorString());
      }
    }
  }


}
