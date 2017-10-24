package com.bbn.bue.gnuplot;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Beta
public final class BoxPlot implements GnuPlottable {

  private final String title;
  private final Axis xAxis;
  private final Axis yAxis;
  private final ImmutableList<Dataset> datasets;
  private final Whiskers whiskers;
  private final double boxWidth;
  private final double pointSize;
  private final boolean showKey;
  private final Grid grid;

  private BoxPlot(final String title, final Axis xAxis, final Axis yAxis,
      final Iterable<Dataset> datasets, final Whiskers whiskers,
      final double boxWidth, final double pointSize, final boolean showKey,
      final Grid grid) {
    this.title = checkNotNull(title);
    this.xAxis = checkNotNull(xAxis);
    this.yAxis = checkNotNull(yAxis);
    this.datasets = ImmutableList.copyOf(datasets);
    this.whiskers = checkNotNull(whiskers);
    this.boxWidth = boxWidth;
    checkArgument(boxWidth >= 0.0, "Box width must be non-negative");
    this.pointSize = pointSize;
    checkArgument(pointSize >= 0.0, "Point size must be non-negative");
    this.showKey = showKey;
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

  @Override
  public PlotBundle toPlotBundle() {
    final PlotBundle.Builder ret = PlotBundle.builder();
    plotCommands(ret);
    return ret.build();
  }

  public static class Dataset {

    private final String name;
    private final double[] data;

    private Dataset(String name, double[] data) {
      this.name = checkNotNull(name);
      this.data = data;
      checkArgument(this.data.length != 0, "Cannot create an empty dataset");
    }

    public static Dataset createCopyingData(String name, double[] data) {
      return new Dataset(name, Arrays.copyOf(data, data.length));
    }

    public static Dataset createAdoptingData(String name, double[] data) {
      return new Dataset(name, data);
    }

    public String name() {
      return name;
    }

    public int numPoints() {
      return data.length;
    }

    public double get(int idx) {
      return data[idx];
    }
  }


  private void plotCommands(final PlotBundle.Builder pbBuilder) {
    pbBuilder.append("set style fill solid 0.25 border 0\n");
    pbBuilder.append("set title\"").append(title).append("\"\n");
    xAxis.appendCommands(pbBuilder);
    yAxis.appendCommands(pbBuilder);
    pbBuilder.append("set style boxplot ");
    pbBuilder.append(whiskers.extentMode().command()).append(" ");
    if (whiskers.showOutliers()) {
      pbBuilder.append("outliers ");
    }
    pbBuilder.append("pointtype 7");
    pbBuilder.append("\n");
    pbBuilder.append("set style data boxplot\n");
    pbBuilder.append("set boxwidth ").append(boxWidth).append("\n");
    pbBuilder.append("set pointsize ").append(pointSize).append("\n");
    if (!showKey) {
      pbBuilder.append("unset key\n");
    }
    //sb.append("set grid ytics lc rgb \"#bbbbbb\" lw 1 lt 0\n");
    grid.appendPlotCommands(pbBuilder);
    addXticsCommand(pbBuilder);
    addPlotCommand(pbBuilder);
  }

  private void addXticsCommand(PlotBundle.Builder pbBuilder) {
    pbBuilder.append("set xtics(");
    for (int i = 0; i < datasets.size(); ++i) {
      if (i != 0) {
        pbBuilder.append(", ");
      }
      pbBuilder.append("\"").append(datasets.get(i).name())
          .append("\" ").append(i);
    }
    pbBuilder.append(") scale 0.0\n");
  }

  private void addPlotCommand(final PlotBundle.Builder sb) {
    sb.append("plot '").appendData(data()).append("'");
    for (int i = 0; i < datasets.size(); ++i) {
      if (i != 0) {
        sb.append(", ''");
      }
      sb.append(" using (").append(i).append("):").append(i + 1);
    }
    sb.append("\n");
  }

  // gnuplot annoyingly requires data to be written in columns
  private String data() {
    if (datasets.isEmpty()) {
      return "";
    } else {
      final StringBuilder ret = new StringBuilder();
      int biggestSize = 0;
      for (final Dataset dataset : datasets) {
        biggestSize = Math.max(biggestSize, dataset.numPoints());
      }
      for (int row = 0; row < biggestSize; ++row) {
        ret.append(valueOrBlank(datasets.get(0), row));
        // all dataset vlaues but first are prefixed with tab
        for (final Dataset dataset : Iterables.skip(datasets, 1)) {
          ret.append("\t");
          ret.append(valueOrBlank(dataset, row));
        }
        ret.append("\n");
      }
      return ret.toString();
    }
  }

  // used by writeData
  private String valueOrBlank(Dataset dataset, int idx) {
    if (idx < dataset.numPoints()) {
      return Double.toString(dataset.get(idx));
    } else {
      return "";
    }
  }


  public static final class Builder {

    private String title = "";
    private final ImmutableList.Builder<Dataset> datasets = ImmutableList.builder();
    private Whiskers whiskers = Whiskers.createDefault();
    private double boxWidth = 0.5;
    private double pointSize = 0.5;
    private boolean showKey = true;
    private Axis xAxis = Axis.xAxis().build();
    private Axis yAxis = Axis.yAxis().build();
    private Grid grid = NormalGrid.builder().build();

    public BoxPlot build() {
      return new BoxPlot(title, xAxis, yAxis, datasets.build(), whiskers,
          boxWidth, pointSize, showKey, grid);
    }

    public Builder addDataset(Dataset dataset) {
      datasets.add(dataset);
      return this;
    }

    public Builder setTitle(String title) {
      this.title = title;
      return this;
    }

    public Builder setXAxis(Axis xAxis) {
      checkArgument(xAxis.axisType == AxisType.X);
      this.xAxis = checkNotNull(xAxis);
      return this;
    }

    public Builder setYAxis(Axis yAxis) {
      checkArgument(yAxis.axisType == AxisType.Y);
      this.yAxis = checkNotNull(yAxis);
      return this;
    }

    public Builder setWhiskers(Whiskers whiskers) {
      this.whiskers = checkNotNull(whiskers);
      return this;
    }

    public void setBoxWidth(double boxWidth) {
      this.boxWidth = boxWidth;
    }

    public Builder setPointSize(double pointSize) {
      this.pointSize = pointSize;
      return this;
    }

    public Builder showKey() {
      this.showKey = true;
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
  }

  public static final class Whiskers {

    private final ExtentMode extentMode;
    private final boolean showOutliers;

    public Whiskers(ExtentMode extentMode, boolean showOutliers) {
      this.extentMode = checkNotNull(extentMode);
      this.showOutliers = showOutliers;
    }

    public ExtentMode extentMode() {
      return extentMode;
    }

    public boolean showOutliers() {
      return showOutliers;
    }

    public static Whiskers createDefault() {
      return builder().build();
    }

    public static Builder builder() {
      return new Builder();
    }

    public interface ExtentMode {

      public String command();
    }

    public static final class InterquartileRange implements ExtentMode {

      private final double range;

      private InterquartileRange(double range) {
        checkArgument(range > 0.0);
        this.range = range;
      }

      public static InterquartileRange of(double range) {
        return new InterquartileRange(range);
      }

      @Override
      public String command() {
        return "range " + range;
      }
    }

    public static final class Fraction implements ExtentMode {

      private final double fraction;

      private Fraction(double fraction) {
        checkArgument(fraction >= 0.0 && fraction <= 1.0);
        this.fraction = fraction;
      }

      public static Fraction of(double fraction) {
        return new Fraction(fraction);
      }

      @Override
      public String command() {
        return "fraction " + fraction;
      }
    }

    public static final class Builder {

      private ExtentMode extentMode = InterquartileRange.of(1.5);
      private boolean showOutliers = true;

      private Builder() {
      }

      public Builder hideOutliers() {
        showOutliers = false;
        return this;
      }

      public Builder setExtentMode(ExtentMode extentMode) {
        this.extentMode = checkNotNull(extentMode);
        return this;
      }

      public Whiskers build() {
        return new Whiskers(extentMode, showOutliers);
      }
    }
  }

  /**
   * Little test program
   */
  public static void main(String[] argv) throws IOException {
    final File outputDir = new File(argv[0]);

    final Random rand = new Random();
    final double mean1 = 5.0;
    final double mean2 = 7.0;
    final double dev1 = 2.0;
    final double dev2 = 4.0;

    final double[] data1 = new double[100];
    final double[] data2 = new double[100];

    for (int i = 0; i < 100; ++i) {
      data1[i] = rand.nextGaussian() * dev1 + mean1;
      data2[i] = rand.nextGaussian() * dev2 + mean2;
    }

    BoxPlot.builder()
        .addDataset(Dataset.createAdoptingData("A", data1))
        .addDataset(Dataset.createAdoptingData("B", data2))
        .setTitle("A vs B")
        .setXAxis(Axis.xAxis().setLabel("FooCategory").build())
        .setYAxis(Axis.yAxis().setLabel("FooValue").setRange(Range.closed(0.0, 15.0)).build())
        .hideKey()
        .build().renderToEmptyDirectory(outputDir);
  }
}
