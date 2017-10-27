package com.bbn.bue.gnuplot;

import com.bbn.bue.common.StringUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.bbn.bue.gnuplot.GnuPlotUtils.gnuPlotString;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A two-dimensional heatmap.  In a heatmap, there are two independent variables, on on each axis,
 * and the points at their intersection are colored according to the value of a third, dependent
 * variable.
 */
public final class HeatMap implements GnuPlottable {

  // horribly inefficient, but this shouldn't be much data
  // and I don't want a dependency on our matrix libraries
  private final ImmutableList<ImmutableList<Double>> data;
  private final String title;
  private final Palette palette;
  private final Axis xAxis;
  private final Axis yAxis;

  private HeatMap(List<? extends List<Double>> data, /*Nullable*/ String title,
      Palette palette, Axis xAxis, Axis yAxis) {
    this.title = title;
    this.xAxis = checkNotNull(xAxis);
    this.yAxis = checkNotNull(yAxis);

    final ImmutableList.Builder<ImmutableList<Double>> dataCopy = ImmutableList.builder();
    int rowLength = 0;
    if (!data.isEmpty()) {
      rowLength = data.get(0).size();
    }
    for (final List<Double> row : data) {
      dataCopy.add(ImmutableList.copyOf(row));
      checkArgument(row.size() == rowLength, "Heat map data contains "
          + "rows of different lengths {} and {}", rowLength, row.size());
    }
    this.data = dataCopy.build();
    this.palette = checkNotNull(palette);
  }

  public File renderToEmptyDirectory(File outputDirectory) throws IOException {
    throw new RuntimeException("update your code to use toPlotBundle");
  }

  public static Builder builder(List<? extends List<Double>> data) {
    return new Builder(data);
  }

  @Override
  public PlotBundle toPlotBundle() {
    final PlotBundle.Builder ret = PlotBundle.builder();
    plotCommands(ret);
    return ret.build();
  }

  /**
   * Builds {@link com.bbn.bue.gnuplot.HeatMap}s.
   */
  public static final class Builder {

    private final List<? extends List<Double>> data;
    private String title = null;
    private Palette palette = Palette.PARULA;
    private Axis xAxis = Axis.xAxis().build();
    private Axis yAxis = Axis.yAxis().build();

    private Builder(List<? extends List<Double>> data) {
      this.data = checkNotNull(data);
    }

    public Builder setTitle(String title) {
      this.title = checkNotNull(title);
      return this;
    }

    public Builder makeGreyscale() {
      this.palette = Palette.GREY;
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

    public HeatMap build() {
      return new HeatMap(data, title, palette, xAxis, yAxis);
    }
  }

  public void addPlotCommand(PlotBundle.Builder pb) {
    StringBuilder sb = new StringBuilder();
    for (final List<Double> row : data) {
      sb.append(StringUtils.spaceJoiner().join(row));
      sb.append("\n");
    }
     /* for (int row = 0; row < data.size(); ++row) {
        final List<Double> rowData = data.get(row);
        for (int col =0; col<rowData.size(); ++col) {
          sb.append(row + " " + col + " " + rowData.get(col)+"\n");
        }
      }*/
    pb.appendData(sb.toString());
  }

  private void plotCommands(PlotBundle.Builder sb) {
    if (title != null) {
      sb.append("set title ").append(gnuPlotString(title)).append("\n");
    }
    sb.append(palette.command()).append("\n");
    xAxis.appendCommands(sb);
    yAxis.appendCommands(sb);
      /*sb.append("set view map\n");
      sb.append("set dgrid3d\n");
      sb.append("splot 'data' using 1:2:3 with pm3d\n");*/
    sb.append("plot '");
    addPlotCommand(sb);
    sb.append("' matrix with image");
  }

  // test code
  public static void main(String[] argv) throws IOException, InterruptedException {
    final File gnuPlotBin = new File(argv[0]);
    final List<ImmutableList<Double>> data = ImmutableList.of(
        ImmutableList.of(0.0, 5.0, 2.0, 0.0, 1.0),
        ImmutableList.of(1.0, 4.0, 3.0, 0.0, 2.0),
        ImmutableList.of(5.0, 2.0, 0.0, 1.0, 1.0),
        ImmutableList.of(9.0, 4.0, 1.0, 3.0, 0.0),
        ImmutableList.of(14.0, 6.0, 2.0, 0.0, 3.0));
    final GnuPlotRenderer renderer = GnuPlotRenderer.createForGnuPlotExecutable(gnuPlotBin);
    final HeatMap heatMap = HeatMap.builder(data).setTitle("This is a heat map").build();
    final File tmpDir = Files.createTempDir();
    renderer.render(heatMap.renderToEmptyDirectory(tmpDir), new File(tmpDir, "heatmap.png"));
  }

  private enum Palette {
    GREY {
      @Override
      public String command() {
        return "set palette grey";
      }
    },
    /**
     * PARULA is the colormap in the newest versions of Matlab. It is designed to address
     * interpretability problems and grey-scaling issues of rainbow color maps (e.g. Matlab Jet.).
     * See Steve Eddins, "Rainbow Color Map Critiques: An Overview and Annotated Bibliography.
     * http://www.mathworks.com/tagteam/81137_92238v00_RainbowColorMap_57312.pdf.
     *
     * Adaptation of Parula to gnuplot is from https://github.com/Gnuplotting/gnuplot-palettes/blob/master/parula.pal
     */
    PARULA {
      @Override
      public String command() {
        return "set palette defined (\\\n"
            + "0 0.2081 0.1663 0.5292,\\\n"
            + "1 0.2116 0.1898 0.5777,\\\n"
            + "2 0.2123 0.2138 0.6270,\\\n"
            + "3 0.2081 0.2386 0.6771,\\\n"
            + "4 0.1959 0.2645 0.7279,\\\n"
            + "5 0.1707 0.2919 0.7792,\\\n"
            + "6 0.1253 0.3242 0.8303,\\\n"
            + "7 0.0591 0.3598 0.8683,\\\n"
            + "8 0.0117 0.3875 0.8820,\\\n"
            + "9 0.0060 0.4086 0.8828,\\\n"
            + "10 0.0165 0.4266 0.8786,\\\n"
            + "11 0.0329 0.4430 0.8720,\\\n"
            + "12 0.0498 0.4586 0.8641,\\\n"
            + "13 0.0629 0.4737 0.8554,\\\n"
            + "14 0.0723 0.4887 0.8467,\\\n"
            + "15 0.0779 0.5040 0.8384,\\\n"
            + "16 0.0793 0.5200 0.8312,\\\n"
            + "17 0.0749 0.5375 0.8263,\\\n"
            + "18 0.0641 0.5570 0.8240,\\\n"
            + "19 0.0488 0.5772 0.8228,\\\n"
            + "20 0.0343 0.5966 0.8199,\\\n"
            + "21 0.0265 0.6137 0.8135,\\\n"
            + "22 0.0239 0.6287 0.8038,\\\n"
            + "23 0.0231 0.6418 0.7913,\\\n"
            + "24 0.0228 0.6535 0.7768,\\\n"
            + "25 0.0267 0.6642 0.7607,\\\n"
            + "26 0.0384 0.6743 0.7436,\\\n"
            + "27 0.0590 0.6838 0.7254,\\\n"
            + "28 0.0843 0.6928 0.7062,\\\n"
            + "29 0.1133 0.7015 0.6859,\\\n"
            + "30 0.1453 0.7098 0.6646,\\\n"
            + "31 0.1801 0.7177 0.6424,\\\n"
            + "32 0.2178 0.7250 0.6193,\\\n"
            + "33 0.2586 0.7317 0.5954,\\\n"
            + "34 0.3022 0.7376 0.5712,\\\n"
            + "35 0.3482 0.7424 0.5473,\\\n"
            + "36 0.3953 0.7459 0.5244,\\\n"
            + "37 0.4420 0.7481 0.5033,\\\n"
            + "38 0.4871 0.7491 0.4840,\\\n"
            + "39 0.5300 0.7491 0.4661,\\\n"
            + "40 0.5709 0.7485 0.4494,\\\n"
            + "41 0.6099 0.7473 0.4337,\\\n"
            + "42 0.6473 0.7456 0.4188,\\\n"
            + "43 0.6834 0.7435 0.4044,\\\n"
            + "44 0.7184 0.7411 0.3905,\\\n"
            + "45 0.7525 0.7384 0.3768,\\\n"
            + "46 0.7858 0.7356 0.3633,\\\n"
            + "47 0.8185 0.7327 0.3498,\\\n"
            + "48 0.8507 0.7299 0.3360,\\\n"
            + "49 0.8824 0.7274 0.3217,\\\n"
            + "50 0.9139 0.7258 0.3063,\\\n"
            + "51 0.9450 0.7261 0.2886,\\\n"
            + "52 0.9739 0.7314 0.2666,\\\n"
            + "53 0.9938 0.7455 0.2403,\\\n"
            + "54 0.9990 0.7653 0.2164,\\\n"
            + "55 0.9955 0.7861 0.1967,\\\n"
            + "56 0.9880 0.8066 0.1794,\\\n"
            + "57 0.9789 0.8271 0.1633,\\\n"
            + "58 0.9697 0.8481 0.1475,\\\n"
            + "59 0.9626 0.8705 0.1309,\\\n"
            + "60 0.9589 0.8949 0.1132,\\\n"
            + "61 0.9598 0.9218 0.0948,\\\n"
            + "62 0.9661 0.9514 0.0755,\\\n"
            + "63 0.9763 0.9831 0.0538)";
      }
    };

    public abstract String command();
  }

}
