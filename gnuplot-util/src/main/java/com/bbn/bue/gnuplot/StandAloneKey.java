package com.bbn.bue.gnuplot;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * A standalone key that exists more or less solely for a shared key for LinePlots in Plotgrid
 *
 * General form borrowed from http://stackoverflow.com/questions/14712251/place-key-below-multiplot-graph-in-gnuplot
 */
@Beta
public class StandAloneKey implements GnuPlottable {

  private final Key key;
  private final List<String> titles;

  private StandAloneKey(final Key key, final List<String> titles) {
    this.key = key;
    this.titles = ImmutableList.copyOf(titles);
  }

  @Override
  public PlotBundle toPlotBundle() {
    PlotBundle.Builder b = PlotBundle.builder();
    b.append("\nset key center center\n");
    b.append("\nset border 0\n");
    b.append("\nunset tics\n");
    b.append("\nunset xlabel\n");
    b.append("\nunset ylabel\n");
    b.append("\nset title \" \"\n");
    b.append("\nset yrange [0:1]\n");

    if(key != null) {
      key.appendPlotCommands(b);
    }

    StringBuilder sb = new StringBuilder("plot ");
    for(String title: titles) {
      sb.append(" 2 t '");
      sb.append(title);
      sb.append("' with lp, \\\n");
    }

    b.append(sb.toString());

    return b.build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Key key = null;
    private List<String> titles = Lists.newArrayList();

    private Builder() {

    }

    public Builder setKey(final Key key) {
      this.key = key;
      return this;
    }

    public Builder setTitles(final List<String> titles) {
      this.titles = ImmutableList.copyOf(titles);
      return this;
    }

    public StandAloneKey build() {
      return new StandAloneKey(key, titles);
    }
  }
}
