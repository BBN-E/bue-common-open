package com.bbn.bue.gnuplot;

import static com.google.common.base.Preconditions.checkNotNull;

public final class NormalGrid implements Grid {

  private final ZIndex zIndex;
  private final LineStyle majorLineStyle;
  private final LineStyle minorLineStyle;
  private final boolean showXMajor;
  private final boolean showXMinor;
  private final boolean showYMajor;
  private final boolean showYMinor;

  private NormalGrid(ZIndex zIndex, LineStyle majorLineStyle, LineStyle minorLineStyle,
      boolean showXMajor,
      boolean showXMinor, boolean showYMajor, boolean showYMinor) {
    this.zIndex = checkNotNull(zIndex);
    this.majorLineStyle = checkNotNull(majorLineStyle);
    this.minorLineStyle = checkNotNull(minorLineStyle);
    this.showXMajor = showXMajor;
    this.showXMinor = showXMinor;
    this.showYMajor = showYMajor;
    this.showYMinor = showYMinor;
  }

  public static Builder builder() {
    return new Builder();
  }

    /*    set grid {{no}{m}xtics} {{no}{m}ytics} {{no}{m}ztics}
    {{no}{m}x2tics} {{no}{m}y2tics}
    {{no}{m}cbtics}
    {polar {<angle>}}
    {layerdefault | front | back}
    { {linestyle <major_linestyle>}
        | {linetype | lt <major_linetype>}
        {linewidth | lw <major_linewidth>}
        { , {linestyle | ls <minor_linestyle>}
            | {linetype | lt <minor_linetype>}
            {linewidth | lw <minor_linewidth>} } }
    unset grid
    show grid*/

  @Override
  public void appendPlotCommands(PlotBundle.Builder sb) {
    sb.append("set grid ");
    sb.append(yesNo(showXMajor)).append("xtics ");
    sb.append(yesNo(showXMinor)).append("mxtics ");
    sb.append(yesNo(showYMajor)).append("ytics ");
    sb.append(yesNo(showYMinor)).append("mytics ");

    sb.append(zIndex.asPlotCommand()).append(" ");
    majorLineStyle.appendPlotCommands(sb);
    sb.append(" , ");
    minorLineStyle.appendPlotCommands(sb);
    sb.append(" \nshow grid\n");
  }

  private String yesNo(boolean b) {
    return b ? "" : "no";
  }

  public static class Builder {

    private static final LineStyle LIGHT_GREY =
        LineStyle.builder().setColor(Color.fromHexString("#AAAAAA")).build();
    private static final LineStyle LIGHTER_GREY =
        LineStyle.builder().setColor(Color.fromHexString("#CCCCCC")).build();

    private ZIndex zIndex = ZIndex.BACK;
    private LineStyle majorLineStyle = LIGHT_GREY;
    private LineStyle minorLineStyle = LIGHTER_GREY;
    private boolean showXMajor = true;
    private boolean showXMinor = true;
    private boolean showYMajor = true;
    private boolean showYMinor = true;

    public Builder bringToFront() {
      this.zIndex = ZIndex.FRONT;
      return this;
    }

    public Builder sendToBack() {
      this.zIndex = ZIndex.BACK;
      return this;
    }

    public Builder setMajorLineStyle(LineStyle majorLineStyle) {
      this.majorLineStyle = checkNotNull(majorLineStyle);
      return this;
    }

    public Builder setMinorLineStyle(LineStyle minorLineStyle) {
      this.minorLineStyle = checkNotNull(minorLineStyle);
      return this;
    }

    public Builder hideMajorXLines() {
      this.showXMajor = false;
      return this;
    }

    public Builder hideMinorXLines() {
      this.showXMinor = false;
      return this;
    }

    public Builder hideMajorYLines() {
      this.showYMajor = false;
      return this;
    }

    public Builder hideMinorYLines() {
      this.showYMinor = false;
      return this;
    }

    public Grid build() {
      return new NormalGrid(zIndex, majorLineStyle, minorLineStyle, showXMajor, showXMinor,
          showYMajor, showYMinor);
    }
  }
}
