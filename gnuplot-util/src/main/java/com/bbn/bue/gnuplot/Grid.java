package com.bbn.bue.gnuplot;

public interface Grid {

  public void appendPlotCommands(PlotBundle.Builder pb);

  enum ZIndex {
    BACK {
      @Override
      public String asPlotCommand() {
        return " back ";
      }
    }, FRONT {
      @Override
      public String asPlotCommand() {
        return " front ";
      }
    };

    public abstract String asPlotCommand();
  }
}
