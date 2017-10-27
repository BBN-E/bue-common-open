package com.bbn.bue.gnuplot;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class PointType {

  private final String gnuPlotString;

  private PointType(String gnuPlotString) {
    this.gnuPlotString = checkNotNull(gnuPlotString);
    checkArgument(!gnuPlotString.isEmpty());
  }

  public static PointType number(int idx) {
    checkArgument(idx >= 0);
    return new PointType(Integer.toString(idx));
  }

  public String asGnuPlotCommand() {
    return gnuPlotString;
  }
}
