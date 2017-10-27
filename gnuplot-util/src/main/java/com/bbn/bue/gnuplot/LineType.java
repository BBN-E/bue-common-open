package com.bbn.bue.gnuplot;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class LineType {

  private final String gnuPlotString;

  private LineType(String gnuPlotString) {
    this.gnuPlotString = checkNotNull(gnuPlotString);
    checkArgument(!gnuPlotString.isEmpty());
  }

  public static LineType number(int idx) {
    checkArgument(idx >= 0);
    return new LineType(Integer.toString(idx));
  }

  public String asGnuPlotCommand() {
    return gnuPlotString;
  }

  public static final LineType DASHED = new LineType("\"dashed\"");
}
