package com.bbn.bue.gnuplot;

import com.google.common.annotations.Beta;
import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Beta
public final class ScatterPlot implements GnuPlottable {

  private ScatterPlot(final String title, final String xLabel, final String yLabel,
      final Range<Double> xRange, final Range<Double> yRange, final double pointSize,
      final List<ScatterData> scatterDatas, final List<String> preCommands) {
    this.title = checkNotNull(title);
    this.xLabel = checkNotNull(xLabel);
    this.yLabel = checkNotNull(yLabel);
    this.xRange = checkNotNull(xRange);
    this.yRange = checkNotNull(yRange);
    this.pointSize = pointSize;
    checkArgument(pointSize >= 0.0);
    this.scatterDatas = ImmutableList.copyOf(scatterDatas);
    this.preCommands = ImmutableList.copyOf(preCommands);
  }

  public static Builder builder() {
    return new Builder();
  }

  @Deprecated
  public void renderToEmptySubdirectory(final File baseDirectory, final String subDirectoryName)
      throws IOException {
    throw new RuntimeException("update your code to use toPlotBundle()");
  }

  private String data()  {
    final StringBuilder sb = new StringBuilder();
    final int maxSize = Collections.max(Lists.transform(scatterDatas, ScatterData.numPointsFunction()));

    for (int row = 0; row < maxSize; ++row) {
      boolean first = true;
      for (int scatter = 0; scatter < scatterDatas.size(); ++scatter) {
        if (!first) {
          sb.append(", ");
        }
        final ScatterData data = scatterDatas.get(scatter);
        final Point2D point;
        if (row < data.points().size()) {
          point = data.points().get(row);
        } else {
          point = data.points().get(data.points().size() - 1);
        }
        sb.append(Double.toString(point.x()));
        sb.append(", ");
        sb.append(Double.toString(point.y()));
        first = false;
      }
      sb.append("\n");
    }

    return sb.toString();
  }

  private static String setString(final String param, final String value) {
    return String.format("set %s \"%s\"\n", param, value);
  }

  private static String setDoubleRange(final String param, final Range<Double> rng) {
    return String.format("set %s [%f:%f]\n", param, rng.lowerEndpoint(), rng.upperEndpoint());
  }

  private void writePlotCommands(final PlotBundle.Builder pb) {
    if (title != null) {
      pb.append(setString("title", title));
    }
    if (xLabel != null) {
      pb.append(setString("xlabel", xLabel));
    }
    if (yLabel != null) {
      pb.append(setString("ylabel", yLabel));
    }
    if (xRange != null) {
      pb.append(setDoubleRange("xrange", xRange));
    }
    if (yRange != null) {
      pb.append(setDoubleRange("yrange", yRange));
    }
    pb.append("set pointsize ");
    pb.append(pointSize);
    pb.append("\n");
    pb.append("set size square\n"); // setting the size/ratio of the graph

    for(String cmd: preCommands) {
      pb.append(cmd);
    }

    final PlotBundle.DatafileReference dataRef = pb.getReferenceFor(data());
    pb.append("plot ");

    int offset = 1;
    boolean first = true;
    for (final ScatterData scatterData : scatterDatas) {
      if (!first) {
        pb.append(", ");
      }

      pb.append("\"");
      pb.appendData(dataRef);
      pb.append("\"");
      pb.append(" using ");
      pb.append(offset);
      pb.append(":");
      pb.append(offset+1);
      pb.append(" title \"");
      pb.append(scatterData.title().or(" "));
      pb.append("\"");
      pb.append(" with points ");
      if(scatterData.color().isPresent()) {
        pb.append("lc rgb " + scatterData.color().get().asQuotedColorString() + " ");
      }


      offset += 2;
      first = false;
    }
    pb.append("\n");
  }

  final String title;
  final String xLabel;
  final String yLabel;
  final Range<Double> xRange;
  final Range<Double> yRange;
  final double pointSize;
  final List<ScatterData> scatterDatas;
  final List<String> preCommands;

  @Override
  public PlotBundle toPlotBundle() {
    final PlotBundle.Builder ret = PlotBundle.builder();
    writePlotCommands(ret);
    return ret.build();
  }

  public static class Builder {

    public Builder() {

    }

    public ScatterPlot build() {
      return new ScatterPlot(title, xLabel, yLabel, xRange, yRange, pointSize, scatterDatas, preCommands);
    }

    public Builder setTitle(final String title) {
      this.title = checkNotNull(title);
      return this;
    }

    public Builder setXLabel(final String xLabel) {
      this.xLabel = checkNotNull(xLabel);
      return this;
    }

    public Builder setYLabel(final String yLabel) {
      this.yLabel = checkNotNull(yLabel);
      return this;
    }

    public Builder setXRange(final Range<Double> range) {
      this.xRange = checkNotNull(range);
      checkArgument(range.hasLowerBound() && range.hasUpperBound());
      checkArgument(range.lowerBoundType() == BoundType.CLOSED);
      checkArgument(range.upperBoundType() == BoundType.CLOSED);
      return this;
    }

    public Builder setYRange(final Range<Double> range) {
      this.yRange = checkNotNull(range);
      checkArgument(range.hasLowerBound() && range.hasUpperBound());
      checkArgument(range.lowerBoundType() == BoundType.CLOSED);
      checkArgument(range.upperBoundType() == BoundType.CLOSED);
      return this;
    }

    public Builder setPointSize(final double pointSize) {
      checkArgument(pointSize >= 0.0);
      this.pointSize = pointSize;
      return this;
    }

    public Builder addScatter(final ScatterData data) {
      scatterDatas.add(checkNotNull(data));
      return this;
    }

    /**
     * add a command to be written after title, sizing stanzas, but before data, such as set xtics
     *
     * this will vanish and we won't tell you
     * @param command
     * @return
     */
    @Beta
    @Deprecated
    public Builder addPreCommand(final String command) {
      preCommands.add(command);
      return this;
    }

    String title = null;
    String xLabel = null;
    String yLabel = null;
    Range<Double> xRange = null;
    Range<Double> yRange = null;
    double pointSize = 1;
    List<ScatterData> scatterDatas = Lists.newArrayList();
    ArrayList<String> preCommands = Lists.newArrayList();
  }

}
