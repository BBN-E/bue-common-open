package com.bbn.bue.gnuplot;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Created by jdeyoung on 5/22/15.
 *
 * Each GnuPlottable passed to this needs some rudimentary awareness of its purpose in life, specifically whether or not it should have a title or legend
 *
 * See http://gnuplot.sourceforge.net/docs_4.2/node203.html for documentation
 *
 * This is a thin wrapper around multi-plot - maybe a less thin wrapper where x,y positions are set
 * could use "blank" plots by default or do the same internal calculations for size/origin that we
 * skip here
 */
@Beta
public final class PlotGrid implements GnuPlottable {

  private final int rows;
  private final int columns;
  private final String title;
  private final boolean rowsFirst;
  private final boolean downwards;
  private final double xscale;
  private final double yscale;
  private final String sizeString;
  private final List<String> startingCommands;
  private final List<PlotBundle> associatedBundles;
  private final int ypixels;
  private final int xpixels;


  private PlotGrid(final int rows, final int columns, final int xpixels, final int ypixels,
      final String title, final boolean rowsFirst, final boolean downwards, final double xscale,
      final double yscale, final String sizeString, final List<String> startingCommands,
      final List<PlotBundle> associatedBundles) {
    this.rows = rows;
    this.columns = columns;
    this.xpixels = xpixels;
    this.ypixels = ypixels;
    this.title = title;
    this.rowsFirst = rowsFirst;
    this.downwards = downwards;
    this.xscale = xscale;
    this.yscale = yscale;
    this.sizeString = sizeString;
    this.startingCommands = startingCommands;
    this.associatedBundles = ImmutableList.copyOf(associatedBundles);
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public PlotBundle toPlotBundle() {
    PlotBundle.Builder pbb = PlotBundle.builder();

    if(xpixels != Integer.MAX_VALUE && ypixels != Integer.MAX_VALUE) {
      pbb.append(String.format("set term png size %d,%d \n", xpixels, ypixels));
    }
    pbb.append(" set multiplot ");
    pbb.append(" layout " + rows + "," + columns);
    if (rowsFirst) {
      pbb.append(" rowsfirst ");
    } else {
      pbb.append(" columnsfirst ");
    }
    if (downwards) {
      pbb.append(" downwards ");
    } else {
      pbb.append(" upwards ");
    }
    if (!Double.isNaN(xscale)) {
      StringBuilder sb = new StringBuilder();
      sb.append(" scale ");
      sb.append(xscale);
      if (!Double.isNaN(yscale)) {
        sb.append(",");
        sb.append(yscale);
      }
      sb.append(" ");
      pbb.append(sb.toString());
    }

    for (String command : startingCommands) {
      pbb.append(command);
      pbb.append(" ");
    }

    pbb.append("\n");
    for (PlotBundle pb : associatedBundles) {
      //pbb.append("\nunset ytics\n"); // multiplot gotcha - axis is carried over between plots!
      pbb.append(sizeString);
      for (Object command : pb.commandComponents()) {
        if (command instanceof PlotBundle.DatafileReference) {
          pbb.appendData((PlotBundle.DatafileReference) command);
        } else {
          pbb.append((String) command);
        }
        // fetching data shouldn't be necessary since plot bundle builder takes data and saves it to commands on appenData
      }
      pbb.append("\n");
    }

    pbb.append("unset multiplot");

    return pbb.build();
  }

  public static class Builder {

    private int rows;
    private int columns;
    private int xpixels;
    private int ypixels;
    private String title;
    private boolean rowsFirst;
    private boolean downwards;
    private double xscale;
    private double yscale;
    private double xsize;
    private double ysize;
    private ArrayList<PlotBundle> associatedBundles;
    private ArrayList<String> startingCommands;

    public Builder() {
      rows = -1;
      columns = -1;
      xpixels = Integer.MAX_VALUE;
      ypixels = Integer.MAX_VALUE;
      rowsFirst = true; // multiplot default
      downwards = true; // multiplot default
      xscale = Double.NaN;
      yscale = Double.NaN;
      xsize = Double.NaN;
      ysize = Double.NaN;
      associatedBundles = new ArrayList<PlotBundle>();
      startingCommands = new ArrayList<String>();
    }

    public Builder setPlotSize(int xpixels, int ypixels) {
      this.xpixels = xpixels;
      this.ypixels = ypixels;
      return this;
    }

    public Builder setRows(int rows) {
      this.rows = rows;
      return this;
    }

    public Builder setColumns(int columns) {
      this.columns = columns;
      return this;
    }

    public Builder setTitle(String title) {
      this.title = title;
      return this;
    }

    public Builder addGnuPlottable(Collection<GnuPlottable> gps) {
      for(GnuPlottable gp: gps) {
        addGnuPlottable(gp);
      }
      return this;
    }

    public Builder addGnuPlottable(GnuPlottable ... gps) {
      for(GnuPlottable gp : gps) {
        addPlotBundle(gp.toPlotBundle());
      }
      return this;
    }

    public Builder addPlotBundle(PlotBundle ... pbs) {
      associatedBundles.addAll(ImmutableList.copyOf(pbs));
      return this;
    }

    public Builder setXScale(double xscale) {
      return setScales(xscale, xscale);
    }

    public Builder setScales(double xscale, double yscale) {
      this.xscale = xscale;
      this.yscale = yscale;
      return this;
    }

    public Builder setSizeForAllPlots(double xsize, double ysize) {
      this.xsize = xsize;
      this.ysize = ysize;
      return this;
    }

    /**
     * default to growing the rows first and then wrapping around to columns, change to false to
     * grow columns first
     */
    public Builder setRowsFirst(boolean rowsFirst) {
      this.rowsFirst = rowsFirst;
      return this;
    }

    /**
     * does the corresponding rowsFirst or columnsFirst grow "up" or "down"? default down.
     */
    public Builder setDownwards(boolean downwards) {
      this.downwards = downwards;
      return this;
    }

    /**
     * Add commands to be executed after setting all other options available here
     */
    @Deprecated
    public Builder addStartingCommands(String... commands) {
      for (String c : commands) {
        startingCommands.add(c);
      }
      return this;
    }

    public PlotGrid build() {
      if (rows == -1 || columns == -1) {
        throw new IllegalArgumentException("rows and columns must be specified");
      }
      if (rows * columns < associatedBundles.size()) {
        // it may make sense to remove this in the future for custom overlapping plots
        throw new IllegalArgumentException(
            "must define at least at most as many plots as there are coordinates");
      }
      final String sizeString;
      if (!Double.isNaN(xsize)) {
        sizeString = "\nset size " + xsize + "," + ysize + "\n";
      } else {
        sizeString = "";
      }
      return new PlotGrid(rows, columns, xpixels, ypixels, title, rowsFirst, downwards, xscale,
          yscale, sizeString,
          ImmutableList.copyOf(startingCommands), ImmutableList.copyOf(associatedBundles));
    }
  }
}
